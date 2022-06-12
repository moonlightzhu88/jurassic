package com.jurassic.core.progress.task;

import java.util.*;

import com.jurassic.core.progress.handler.pin.Pin;
import com.jurassic.core.progress.Progress;
import com.jurassic.core.progress.handler.pin.express.Express;

/**
 * ��ת����
 * ��ʾ������������ת���������
 * 
 * @author yzhu
 *
 */
public class JumpStartTask extends Task {

	public static final String KEY = "jmp_start";

	// ��̬������ʵ����һ������
	private List<Task> _dynamicNextTasks = new ArrayList<>();

	public JumpStartTask(Progress progress, String desc) {
		super(progress, desc);
	}

	public boolean isAuto() {
		return true;
	}

	/**
	 * �����������ݶ�̬�����к��������ѡ�����ִ�е�
	 */
	public List<Task> getNextTasks() {
		for (Task to : this._nextTasks) {
			Express condition = this._conditions.get(to);
			if ((Boolean) condition.getData() == true) {
				this._dynamicNextTasks.add(to);
			}
		}
		return this._dynamicNextTasks;
	}

	public void input(Pin... pins) {

	}

	public Pin output(int pinIdx) {
		return null;
	}

	public String getPackageKey() {
		return this._progress.getPackageKey();
	}

	public String getEventKey() {
		return KEY;
	}

	public void end(Throwable ex) {
		this._status = S_END;
	}

	/**
	 * �ж�task�Ƿ��Ƿ�֧·����
	 */
	public boolean isBranchTask(Task task) {
		for (Task branchTask : this._dynamicNextTasks)
			if (branchTask.hasChild(task))
				return true;
		return false;
	}
}
