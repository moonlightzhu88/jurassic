package com.jurassic.core.progress.worker;

import com.jurassic.core.bus.EventBus;
import com.jurassic.core.event.EventWrapper;
import com.jurassic.core.global.GlobalInstRegisterTable;
import com.jurassic.core.handler.AbstractHandler;
import com.jurassic.core.processor.EventHandler;
import com.jurassic.core.progress.handler.EPU;
import com.jurassic.core.progress.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 事件运行逻辑处理器EPU
 * 负责流程中事件的运算逻辑
 *
 * @author yzhu
 */
public class EPUWorker implements EventHandler<EventWrapper<Task>> {

    private EPU _epu;// 中央处理器
    private final EventBus _eventBus;// 总线

    protected static Logger logger = LoggerFactory.getLogger(EPUWorker.class);

    public EPUWorker() {
        this._eventBus = GlobalInstRegisterTable.getInst(
                EventBus.GLOBAL_KEY, EventBus.class);
    }

    public void onEvent(EventWrapper<Task> wrapper, long sequence,
                        boolean endOfBatch) {
        // epu计算流程的下一步任务
        Task task = wrapper.getEvent();
        try {
            // 计算流程后续执行的事件列表
            this.calculate(task);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            wrapper.reset();
        }
    }

    /**
     * 根据当前的事件计算后续的执行事件
     */
    private void calculate(Task task) {
        // 使用中央处理器计算事件的后续步骤
        List<Task> nextTasks = this._epu.calculate(task);
        // autoEvents统计所有自动执行的事件
        // 例如JumpStartTask之类
        List<Task> autoTasks = new ArrayList<>();
        if (nextTasks != null && !nextTasks.isEmpty()) {
            // 发布流程的新指令
            for (Task _task : nextTasks) {
                if (_task.isAuto()) {
                    // 自动任务,需要再次计算
                    autoTasks.add(_task);
                } else if (_task.getStatus() == 0) {
                    // 非自动执行的任务,将其放到总线上
                    try {
                        this._eventBus.fire(_task.getPackageKey(),
                                _task.getEventKey(), _task);
                    } catch (Throwable e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }

            if (!autoTasks.isEmpty()) {
                // 再次计算自动任务的下一步任务
                for (Task auto : autoTasks) {
                    // 自动任务结束
                    auto.end(null);
                    this.calculate(auto);
                }
            }
        }
    }

    public void setHandler(AbstractHandler handler) {
        this._epu = (EPU) handler;
    }
}
