package com.jurassic.core.progress.handler.impl;

import com.jurassic.core.bus.EventBus;
import com.jurassic.core.global.GlobalInstRegisterTable;
import com.jurassic.core.progress.Progress;
import com.jurassic.core.progress.handler.TaskHandler;
import com.jurassic.core.progress.handler.pin.Pin;
import com.jurassic.core.progress.manager.ProgressManager;
import com.jurassic.core.progress.task.SubProgressInvokeTask;
import com.jurassic.core.progress.task.Task;

/**
 * 调用子流程
 *
 * @author yzhu
 */
public class SubProgressInvokeHandler extends TaskHandler<SubProgressInvokeTask> {

    private final ProgressManager _progressManager =
            GlobalInstRegisterTable.getInst(
            ProgressManager.GLOBAL_KEY, ProgressManager.class);// 流程管理器
    private final EventBus _eventBus = GlobalInstRegisterTable.getInst(
            EventBus.GLOBAL_KEY, EventBus.class);// 总线

    public String getHandlerKey() {
        return SubProgressInvokeTask.KEY;
    }

    public void handle(SubProgressInvokeTask task) throws Throwable {
        if (task.isResumed()) {
            // 子流程执行完毕，可以重启执行
            task.resume();
            // 根据子流程调用的结果进行处理
            Object result = task.output(0).getData();
            if (result instanceof Throwable)
                throw (Throwable) result;// 抛出子流程执行的异常信息
            // 否则正常返回
        } else {
            String subPackageKey = task.getSubPackageKey();
            String subProgressKey = task.getSubProgressKey();
            Pin[] params = task.getParams();
            // 创建子流程
            Progress subProgress = this._progressManager.createProgress(
                    subPackageKey, subProgressKey,
                    params, task.getProgress());
            if (subProgress == null)
                throw new RuntimeException("sub progress[" + subProgressKey
                        + "] invoke fail");
            // 设置子流程的结果通知接口，通知到task本身
            subProgress.setNotification(task);
            // 当前任务暂停等待子流程结束
            task.halt();
            // 开始执行子流程
            try {
                Task start = subProgress.getStartTask();
                this._eventBus.fire(subProgress.getPackageKey(),
                        start.getEventKey(), start);
            } catch (Throwable ex) {
                // 子流程启动失败
                // 原有任务重新启动，并以异常结束
                task.startResume();
                task.resume();
                throw ex;
            }
            // 正常结束，task处于halt状态，等待子流程结束的时候结果通知将其resume
        }
    }
}
