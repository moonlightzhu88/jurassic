package com.jurassic.core.progress.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jurassic.core.event.Event;
import com.jurassic.core.progress.handler.pin.Pin;
import com.jurassic.core.progress.Progress;
import com.jurassic.core.progress.handler.pin.express.Express;

/**
 * ����Ļ���
 * ÿһ��ҵ�����������ɸ��������
 * ÿ�����������һС��ҵ���߼�
 * 
 * @author yzhu
 */
public abstract class Task extends Event {
	protected String _desc;// task�������е���ʾ����
	// ����֮��ͨ��ǰ���ͺ���໥��ϵ
	protected List<Task> _prevTasks = new ArrayList<>();// ǰ������
	protected List<Task> _nextTasks = new ArrayList<>();// �������
	protected Progress _progress;// ��������������
	// ����ִ�еĽ�ֹʱ��,-1��ʾ����û�н�ֹʱ��
	// ÿ�������������һ����ֹ���ʱ��,�ڹ涨ʱ�����޷�������̵�
	// ���ж�����ִ��,�Գ�ʱ�������
	protected long _cutoffTime = -1;
	// �����轻�������handler����������������һЩ�����жϵȻ�����

	public Task(Progress progress, String desc) {
		this._progress = progress;
		this._desc = desc;
		if (progress != null)
			progress.addTask(this);// ÿ�δ���task��ʱ��,����󶨵���Ӧ������
	}

	/**
	 * �ж��Ƿ����Զ�����
	 */
	public abstract boolean isAuto();

	public String getDesc(){
		return this._desc;
	}

	public void setCutOffTime(long time) {
		this._cutoffTime = time;
	}

	public long getCutoffTime() {
		return this._cutoffTime;
	}

	public List<Task> getNextTasks() {
		return this._nextTasks;
	}

	/**
	 * ��next�������ӵ���ǰ�������ִ��
	 */
	public Task bind(Task nextTask) {
		this._nextTasks.add(nextTask);
		nextTask._prevTasks.add(this);

		return nextTask;
	}

	protected Map<Task, Express> _conditions = new HashMap<>();// ��ת·���ϵ��������ʽ
	public Task bindWithCondition(Task nextTask, Express condition) {
		this._nextTasks.add(nextTask);
		nextTask._prevTasks.add(this);
		this._conditions.put(nextTask, condition);

		return nextTask;
	}

	public Progress getProgress() {
		return this._progress;
	}

	/**
	 * �жϵ�ǰ�����Ƿ����ִ��
	 * ����ִ�еı�׼�ǣ���ǰ���������ǰ�������Խ���
	 */
	public boolean isReady() {
		// ��ǰ�����״̬Ϊ��ʼ
		if (this._status != 0)
			return false;

		// ����ǰ�������״̬�����������
		for (Task prev : this._prevTasks) {
			if (!prev.isAuto() && prev._status != S_END) {
				return false;
			}
		}

		return true;
	}

	/**
	 * �ж�task�����Ƿ��ǵ�ǰ�����������������������
	 */
	public boolean hasChild(Task task) {
		if (this == task)
			return true;
		for (Task child : this._nextTasks) {
			if (child.hasChild(task))
				return true;
		}
		return false;
	}

	/**
	 * ������ܽ�����
	 */
	public abstract void input(Pin... pins);

	/**
	 * ���ָ��λ�õ�����ܽ�����
	 */
	public abstract Pin output(int pinIdx);

}
