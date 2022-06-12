package com.jurassic.core.progress.task;

import com.jurassic.core.progress.handler.pin.Pin;
import com.jurassic.core.progress.Progress;

import java.util.List;

/**
 * 跳转分支的结束任务
 * 所有的分支任务必须最终连接到该任务
 * JumpEndTask根据与之关联的JumpStartTask任务之前动态跳转的路径数量
 * 来判断是否所有的分支路径都已完成
 * 它也是一个自动执行任务,无需特定的处理器
 *
 * @author yzhu
 */
public class JumpEndTask extends Task {
	
	public static final String KEY = "jmp_end";
	private JumpStartTask _startOfJmpTask;// 与之对应的跳转任务起始

	public JumpEndTask(Progress progress, String desc) {
		super(progress, desc);
	}

	public void setJumpStartTask(JumpStartTask start) {
		this._startOfJmpTask = start;
	}

	public boolean isAuto() {
		return true;
	}

	/**
	 * 对于分支路径的出口isReady判断,需要找到实际走的那条分支的状态情况
	 */
	public boolean isReady() {
		if (this._status != 0)
			return false;

		for (Task prev : this._prevTasks) {
			if (this._startOfJmpTask.isBranchTask(prev)) {
				if (prev.getStatus() != S_END)
					return false;
			}
		}

		return true;
	}

	/**
	 * 无需输入
	 */
	public void input(Pin... pins) {
	}

	/**
	 * 无需输出
	 */
	public Pin output(int pinIdx) {
		return null;
	}

	public String getPackageKey() {
		return this._progress.getPackageKey();
	}

	public String getEventKey() {
		return KEY;
	}
}
