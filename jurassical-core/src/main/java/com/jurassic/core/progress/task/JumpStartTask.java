package com.jurassic.core.progress.task;

import java.util.*;

import com.jurassic.core.progress.handler.pin.Pin;
import com.jurassic.core.progress.Progress;
import com.jurassic.core.progress.handler.pin.express.Express;

/**
 * 跳转任务
 * 表示流程中条件跳转的任务起点
 * 
 * @author yzhu
 *
 */
public class JumpStartTask extends Task {

	public static final String KEY = "jmp_start";

	// 动态决定的实际下一步任务
	private List<Task> _dynamicNextTasks = new ArrayList<>();

	public JumpStartTask(Progress progress, String desc) {
		super(progress, desc);
	}

	public boolean isAuto() {
		return true;
	}

	/**
	 * 根据输入数据动态在所有后继任务中选择可以执行的
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
	 * 判断task是否是分支路径上
	 */
	public boolean isBranchTask(Task task) {
		for (Task branchTask : this._dynamicNextTasks)
			if (branchTask.hasChild(task))
				return true;
		return false;
	}
}
