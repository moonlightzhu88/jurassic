package com.jurassic.core.dataflow.worker;

import com.jurassic.core.bus.EventBus;
import com.jurassic.core.bus.impl.EBus;
import com.jurassic.core.dataflow.handler.BatchEventHandler;
import com.jurassic.core.event.Event;
import com.jurassic.core.event.EventWrapper;
import com.jurassic.core.global.GlobalInstRegisterTable;
import com.jurassic.core.handler.AbstractHandler;
import com.jurassic.core.processor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量处理器的工作线程
 *
 * @param <T>
 */
public class BatchEventWorker<T extends Event> implements EventHandler<EventWrapper<T>> {

    private static final Logger logger
            = LoggerFactory.getLogger(BatchEventWorker.class);

    private BatchEventHandler<T> _handler;// 业务处理器
    private final EventBus _eventBus;// 总线

    private final List<EventWrapper<T>> _wrapperBuf = new ArrayList<>();// 存储批量处理的输入事件封装类
    private final List<T> _eventBuf = new ArrayList<>();// 批量处理的事件

    public BatchEventWorker() {
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
        if (!this._handler.getHandlerKey().equals(EBus.COMPONENT_KEY_STATICS)) {
            try {
                // 4个默认的处理器，packageKey为空，不会交由其他的组件包部署上下文处理
                this._eventBus.fire(null, EBus.COMPONENT_KEY_STATICS, event);
            } catch (Throwable ex) {
                logger.warn(ex.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void doBatchEvents() {
        Throwable err = null;
        List<T> filterEvents = null;// 存储需要处理的事件
        if (this._handler.hasBeforeFilters()) {
            filterEvents = new ArrayList<>();
            for (T event : this._eventBuf) {
                // 依次对每一个需要处理的输入事件进行Filter
                Event filterEvent = this._handler.beforeHandler(event);
                if (filterEvent != null) {
                    if (!filterEvent.equals(event)) {
                        // 如果Filter处理后事件发生了变化
                        // 则结束输入事件处理，转而处理过滤后的事件
                        try {
                            this._eventBus.fire(
                                    filterEvent.getPackageKey(),
                                    filterEvent.getEventKey(),
                                    filterEvent);
                        } catch (Throwable ex) {
                            logger.warn(ex.getMessage());
                        }
                    } else {
                        // 如果输入事件没有发生变化，则继续后续处理
                        filterEvents.add((T) filterEvent);
                    }
                }
            }
        }
        List<T> dealEvents = filterEvents != null ? filterEvents : this._eventBuf;
        try {
            if (filterEvents != null) {
                // 如果执行过过滤处理，则处理过滤后的事件
                if (!filterEvents.isEmpty()) {
                    this._handler.handle(filterEvents);
                }
            } else {
                // 否则处理原有事件
                this._handler.handle(this._eventBuf);
            }
        } catch (Throwable ex) {
            err = ex.getCause() != null ? ex.getCause() : ex;
            logger.error(ex.getMessage(), ex);
        }
        // 结束原有的输入事件
        for (EventWrapper<T> _wrapper : this._wrapperBuf) {
            this.finish(_wrapper, err);
        }
        if (!this._handler.getHandlerKey().equals(EBus.COMPONENT_KEY_STATICS)) {
            // 对处理的事件根据配置决定是否需要重新调度执行该事件
            for (T _event : dealEvents) {
                if (_event.getScheduleType() == Event.RETRY_ON_ERROR) {
                    if (err != null) {
                        // 失败再执行，重新调度该事件
                        this._eventBus.schedule(_event);
                    }
                } else if (_event.getScheduleType() == Event.MULTIPLE) {
                    // 多次调度，无视本次执行结果如何
                    int retryNum = _event.getRetryNum();
                    if (retryNum > 0) {
                        // 对计数器进行减1操作，直到减到0不再执行
                        _event.setRetryNum(--retryNum);
                        this._eventBus.schedule(_event);
                    } else if (retryNum == -1) {
                        // 对于次数为-1，表示永远反复执行下去
                        this._eventBus.schedule(_event);
                    }
                }
            }
        }
        this._eventBuf.clear();
        this._wrapperBuf.clear();
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

    public void onEvent(EventWrapper<T> wrapper, long sequence,
                        boolean endOfBatch) {
        try {
            T event = wrapper.getEvent();
            // 事件开始处理
            event.run();
            // 将事件加入到批处理缓冲区
            this._eventBuf.add(event);
            this._wrapperBuf.add(wrapper);
            if (endOfBatch) {
                // 一批次结束，可以开始处理了
                this.doBatchEvents();
            }
        } catch (Throwable ex) {
            logger.warn(ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    public void setHandler(AbstractHandler handler) {
        this._handler = (BatchEventHandler<T>) handler;
    }
}
