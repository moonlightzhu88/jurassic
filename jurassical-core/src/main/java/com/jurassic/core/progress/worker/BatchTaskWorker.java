package com.jurassic.core.progress.worker;

import com.jurassic.core.bus.EventBus;
import com.jurassic.core.bus.impl.EBus;
import com.jurassic.core.event.Event;
import com.jurassic.core.event.EventWrapper;
import com.jurassic.core.global.GlobalInstRegisterTable;
import com.jurassic.core.handler.AbstractHandler;
import com.jurassic.core.processor.EventHandler;
import com.jurassic.core.progress.handler.BatchTaskHandler;
import com.jurassic.core.progress.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理多个task的worker
 *
 * @author yzhu
 */
public class BatchTaskWorker<T extends Task> implements EventHandler<EventWrapper<T>> {

    private BatchTaskHandler<T> _handler;// 业务处理器
    private final EventBus _eventBus;// 总线

    private final List<EventWrapper<T>> _wrapperBuf = new ArrayList<>();
    private final List<T> _taskBuf = new ArrayList<>();// 批量处理事件的缓冲区

    protected static Logger logger = LoggerFactory.getLogger(BatchTaskWorker.class);

    public BatchTaskWorker() {
        this._eventBus = GlobalInstRegisterTable.getInst(
                EventBus.GLOBAL_KEY, EventBus.class);
    }

    private void doBatchEvents() {
        // 批处理开始
        Throwable err = null;
        try {
            this._handler.handle(this._taskBuf);
        } catch (Throwable ex) {
            err = ex.getCause() != null ? ex.getCause() : ex;
            logger.error("event " + this._handler.getHandlerKey() + " happen error:"
                    + err.getMessage(), err);
        } finally {
            for (int i = 0; i < this._taskBuf.size(); i++) {
                // 处理完成后将事件重置
                this._wrapperBuf.get(i).reset();
            }
        }
        // 需要将结果通知回总线,继续后续的其他处理逻辑
        if (err != null) {
            for (T task : this._taskBuf) {
                task.end(err);
                this.doNext(task);
            }
        } else {
            for (T task : this._taskBuf) {
                // 正常流程下,如果事件被暂停,则后续不对该事件进行处理
                // 直到处理器对该事件作出正常结束的操作
                if (task.getStatus() != Event.S_HALT) {
                    task.end(null);
                    this.doNext(task);
                }
            }
        }
        // 清空缓冲区
        this._taskBuf.clear();
        this._wrapperBuf.clear();
    }

    public void onEvent(EventWrapper<T> wrapper, long sequence,
                        boolean endOfBatch) {
        try {
            T task = wrapper.getEvent();
            // 事件开始处理
            task.run();
            // 先将事件加入到缓冲区
            this._taskBuf.add(task);
            this._wrapperBuf.add(wrapper);
            if (endOfBatch) {
                this.doBatchEvents();
            }
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
        this._handler = (BatchTaskHandler<T>) handler;
    }
}
