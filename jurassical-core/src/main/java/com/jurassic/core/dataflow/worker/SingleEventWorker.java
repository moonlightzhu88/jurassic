package com.jurassic.core.dataflow.worker;

import com.jurassic.core.bus.EventBus;
import com.jurassic.core.bus.impl.EBus;
import com.jurassic.core.dataflow.handler.EventHandler;
import com.jurassic.core.event.Event;
import com.jurassic.core.event.EventWrapper;
import com.jurassic.core.global.GlobalInstRegisterTable;
import com.jurassic.core.handler.AbstractHandler;
import com.jurassic.core.processor.WorkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 处理器的工作线程
 *
 * @param <T>
 */
public class SingleEventWorker<T extends Event> implements WorkHandler<EventWrapper<T>> {

    private EventHandler<T> _handler;// 处理器
    private final EventBus _eventBus;// 总线

    protected static final Logger logger
            = LoggerFactory.getLogger(SingleEventWorker.class);

    public SingleEventWorker() {
        this._eventBus = GlobalInstRegisterTable.getInst(
                EventBus.GLOBAL_KEY, EventBus.class);
    }

    /**
     * 事件完成处理
     * 执行一些事件处理完成后的收尾工作
     */
    protected void finish(EventWrapper<T> wrapper, Throwable error) {
        T event = wrapper.getEvent();
        // 标记事件处理结束，记录错误异常，事件状态以及执行信息等
        event.end(error);
        // 清空事件队列中的数据
        wrapper.reset();
        try {
            // 4个默认的处理器，packageKey为空，不会交由其他的组件包部署上下文处理
            this._eventBus.fire(null, EBus.COMPONENT_KEY_STATICS, event);
        } catch (Throwable ex) {
            logger.warn(ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void doEvent(EventWrapper<T> wrapper) {
        T event = wrapper.getEvent();
        // 事件开始处理
        event.run();

        // 记录指令开始处理时间
        Throwable err = null;
        Event filterEvent = event;
        if (this._handler.hasBeforeFilters()) {
            // 对输入事件进行过滤处理
            filterEvent = this._handler.beforeHandler(filterEvent);
            if (filterEvent == null) {
                // 如果事件被Filter屏蔽了，则直接结束
                this.finish(wrapper, null);
                return;
            } else {
                if (!filterEvent.equals(event)) {
                    // 如果Filter处理后事件发生了变化
                    // 则结束输入事件处理，转而处理过滤后的事件
                    this.finish(wrapper, null);
                    try {
                        this._eventBus.fire(
                                filterEvent.getPackageKey(),
                                filterEvent.getEventKey(),
                                filterEvent);
                    } catch (Throwable ex) {
                        logger.warn(ex.getMessage());
                    }
                    return;
                }
            }
        }
        try {
            this._handler.handle((T)filterEvent);
        } catch (Throwable ex) {
            err = ex.getCause() != null ? ex.getCause() : ex;
            logger.error(ex.getMessage(), ex);
        }
        // 处理完event后结束输入事件
        this.finish(wrapper, err);

        // 根据事件的配置来决定是否需要重新调度执行该事件
        if (event.getScheduleType() == Event.RETRY_ON_ERROR) {
            if (err != null) {
                // 失败再执行，重新调度该事件
                this._eventBus.schedule(event);
            }
        } else if (event.getScheduleType() == Event.MULTIPLE) {
            // 多次调度，无视本次执行结果如何
            int retryNum = event.getRetryNum();
            if (retryNum > 0) {
                // 调度次数大于0的可以再次调度，否则结束
                event.setRetryNum(--retryNum);
                this._eventBus.schedule(event);
            } else if (retryNum == -1) {
                // 对于次数为-1，表示永远反复执行下去
                this._eventBus.schedule(event);
            }
        }
        if (err != null) {
            // 如果处理发生错误，则结束，并且清空输出事件（之前的无论什么情况都不会被触发）
            this._handler.clearOutputEvents();
            return;
        }
        // 处理输出事件
        List<Event> outputEvents = this._handler.getOutputEvents();
        if (outputEvents != null && !outputEvents.isEmpty()) {
            for (Event outputEvent : outputEvents) {
                try {
                    // 对输出事件进行过滤
                    if (this._handler.hasAfterFilters()) {
                        outputEvent = this._handler.afterHandler(outputEvent);
                    }
                    if (outputEvent != null) {
                        this._eventBus.fire(
                                outputEvent.getPackageKey(),
                                outputEvent.getEventKey(),
                                outputEvent);
                    }
                } catch (Throwable ex) {
                    logger.warn(ex.getMessage(), ex);
                }
            }
            this._handler.clearOutputEvents();
        }
    }

    public void onEvent(EventWrapper<T> wrapper) {
        try {
            this.doEvent(wrapper);
        } catch (Throwable ex) {
            logger.warn(ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    public void setHandler(AbstractHandler handler) {
        this._handler = (EventHandler<T>) handler;
    }
}
