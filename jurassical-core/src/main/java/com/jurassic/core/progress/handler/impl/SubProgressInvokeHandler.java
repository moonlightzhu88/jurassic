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
 * ����������
 *
 * @author yzhu
 */
public class SubProgressInvokeHandler extends TaskHandler<SubProgressInvokeTask> {

    private final ProgressManager _progressManager =
            GlobalInstRegisterTable.getInst(
            ProgressManager.GLOBAL_KEY, ProgressManager.class);// ���̹�����
    private final EventBus _eventBus = GlobalInstRegisterTable.getInst(
            EventBus.GLOBAL_KEY, EventBus.class);// ����

    public String getHandlerKey() {
        return SubProgressInvokeTask.KEY;
    }

    public void handle(SubProgressInvokeTask task) throws Throwable {
        if (task.isResumed()) {
            // ������ִ����ϣ���������ִ��
            task.resume();
            // ���������̵��õĽ�����д���
            Object result = task.output(0).getData();
            if (result instanceof Throwable)
                throw (Throwable) result;// �׳�������ִ�е��쳣��Ϣ
            // ������������
        } else {
            String subPackageKey = task.getSubPackageKey();
            String subProgressKey = task.getSubProgressKey();
            Pin[] params = task.getParams();
            // ����������
            Progress subProgress = this._progressManager.createProgress(
                    subPackageKey, subProgressKey,
                    params, task.getProgress());
            if (subProgress == null)
                throw new RuntimeException("sub progress[" + subProgressKey
                        + "] invoke fail");
            // ���������̵Ľ��֪ͨ�ӿڣ�֪ͨ��task����
            subProgress.setNotification(task);
            // ��ǰ������ͣ�ȴ������̽���
            task.halt();
            // ��ʼִ��������
            try {
                Task start = subProgress.getStartTask();
                this._eventBus.fire(subProgress.getPackageKey(),
                        start.getEventKey(), start);
            } catch (Throwable ex) {
                // ����������ʧ��
                // ԭ���������������������쳣����
                task.startResume();
                task.resume();
                throw ex;
            }
            // ����������task����halt״̬���ȴ������̽�����ʱ����֪ͨ����resume
        }
    }
}
