package com.jurassic.core.progress.worker;

import com.jurassic.core.bus.EventBus;
import com.jurassic.core.bus.impl.EBus;
import com.jurassic.core.event.Event;
import com.jurassic.core.event.EventWrapper;
import com.jurassic.core.global.GlobalInstRegisterTable;
import com.jurassic.core.handler.AbstractHandler;
import com.jurassic.core.processor.WorkHandler;
import com.jurassic.core.progress.handler.TaskHandler;
import com.jurassic.core.progress.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 处理单个Task的worker
 *
 * @param <T>
 */
public class SingleTaskWorker<T extends Task> implements WorkHandler<EventWrapper<T>> {

    private TaskHandler<T> _handler;// 业务处理器
    private final EventBus _eventBus;// 总线

    protected static Logger logger = LoggerFactory.getLogger(SingleTaskWorker.class);

    public SingleTaskWorker() {
        this._eventBus = GlobalInstRegisterTable.getInst(
                EventBus.GLOBAL_KEY, EventBus.class);
    }

    /**
     * 处理任务
     */
    private void doTask(EventWrapper<T> wrapper) {
        T task = wrapper.getEvent();
        // 登记处理时间并标记事件开始处理
        task.run();
        Throwable err = null;
        try {
            // 业务处理
            this._handler.handle(task);
        } catch (Throwable ex) {
            err = ex.getCause() != null ? ex.getCause() : ex;
            logger.error(
                    "event " + this._handler.getHandlerKey() + " happen error:"
                            + err.getMessage(), err);
        } finally {
            // 处理完成后将事件重置
            wrapper.reset();
        }
        // 需要将结果通知回总线,继续后续的其他处理逻辑
        if (err != null) {
            task.end(err);
            this.doNext(task);
        } else {
            // 正常流程下,如果事件被暂停,则后续不对该事件进行处理
            // 直到处理器对该事件作出正常结束的操作
            if (task.getStatus() != Event.S_HALT) {
                task.end(null);
                this.doNext(task);
            }
        }
    }

    public void onEvent(EventWrapper<T> wrapper) {
        try {
            this.doTask(wrapper);
        } catch (Throwable ex) {
            logger.warn(ex.getMessage(), ex);
        }
    }

    /**
     * 当前任务执行完毕（无论成功或异常）
     * 执行下一步操作
     */
    private void doNext(Task task) {
        try {
            // 将当前task交由epu，计算流程的下一步
            if (task.getProgress() != null)
                this._eventBus.fire(
                        task.getProgress().getPackageKey(),
                        "epu", task);
        } catch(Throwable ex) {
            logger.warn(ex.getMessage(), ex);
        }
        try {
            // 将当前任务交由static处理器，执行信息的统计
            this._eventBus.fire(
                    null, EBus.COMPONENT_KEY_STATICS, task);
        } catch(Throwable ex) {
            logger.warn(ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    public void setHandler(AbstractHandler handler) {
        this._handler = (TaskHandler<T>) handler;
    }
}
