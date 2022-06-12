package com.jurassic.core.progress.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.jurassic.core.bus.Constant;
import com.jurassic.core.handler.AbstractHandler;
import com.jurassic.core.progress.Progress;
import com.jurassic.core.progress.task.Task;

/**
 * ���봦����
 * ��Ҫ�����¼��������߼�
 * ��ÿһ���¼�������Ϻ�,�����봦�������������Ҫִ�е��¼�
 * ÿһ�������Ӧ�ð�ԭ������Ҫ�ṩΨһ�����봦����
 * 
 * @author yzhu
 */
public class EPU extends AbstractHandler {

	public EPU() {
		super();
		this._numOfThread = 1;
		this._powerOfBuffer = Constant.DEFAULT_EPU_POWER;
	}

	public String getHandlerKey() {
		return "epu";
	}

	/**
	 * �������̵���һ������
	 */
	public List<Task> calculate(Task currentTask) {
		Throwable error = currentTask.getError();
		if (currentTask.getProgress().isInterrupted()
				&& currentTask != currentTask.getProgress().getEndTask()) {
			// ����������������Ѿ��ж�,����Ը�����,���ټ���ִ��
			// ���̵�end taskһ��Ҫ��ִ��
			return null;
		}

		List<Task> nextTasks = new ArrayList<>();
		if (error != null) {
			// ͨ���쳣�����������̵���һ������
			nextTasks.add(this.interrupt(currentTask, error));
			return nextTasks;
		}

		boolean outOfTime = currentTask.getCutoffTime() != -1
				&& System.currentTimeMillis() > currentTask.getCutoffTime();
		// �������ִ��ʱ��,�����������ʱ��,�����񱻱��Ϊ��ʱ,���������񽫲���ִ����ȫ�����Ϊcancel
		if (outOfTime) {
			// ִ�г�ʱ,���׳�timeout�쳣,��ֹ��������ִ��,�����ؿͻ��˸��쳣
			nextTasks.add(this.interrupt(currentTask, new TimeoutException("timeout")));
			return nextTasks;
		}

		if (currentTask == currentTask.getProgress().getEndpoint()) {
			// �����������ʵ���������ˣ�������������Ϣ��
			currentTask.getProgress().notifyResult();
		}

		// �������ǰ����,�������̵���һ��
		List<Task> tasks = currentTask.getNextTasks();
		if (tasks != null) {
			for (Task next : tasks) {
				if (next.isReady()) {
					// �����һ��������׼���ã�����Լ���ִ��
					nextTasks.add(next);
				}
			}
		}
		return nextTasks;
	}

	/**
	 * ���������ڷ����жϵ�ʱ��Ӧ��ִ�е�����
	 */
	protected Task interrupt(Task interruptedTask, Throwable ex) {

		Progress progress = interruptedTask.getProgress();
		// ���̵����ж�,�������ڴ����κθ������µ�task����,����calculate�����Ŀ�ʼ�ж��߼�
		progress.happenException();
		// ֪ͨ�쳣����
		progress.notifyError(ex);
		// �����Ƿ���ƥ���catchTable
		Task entry = progress.findErrorEntry(interruptedTask);
		if (entry == null) {
			// ���û��ƥ��ɹ�,Ĭ����һ��taskΪend task
			entry = progress.getEndTask();
		}
		return entry;
	}

}
