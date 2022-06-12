package com.jurassic.core.progress.task;

import com.jurassic.core.progress.handler.pin.Pin;
import com.jurassic.core.progress.Progress;

/**
 * 流程的结束任务
 * name和key属性都采用流程名称+_end的形式
 * 用来区分各个不同流程的开始任务
 *
 * @author yzhu
 */
public class EndTask extends Task {
	// 各个业务流程有各自不同的End节点,他们的key以_end结尾,以整个业务流程的key作为开头
	public static final String END_SUFFIX = "_end";

	public EndTask(Progress progress) {
		super(progress, "");
	}

	public boolean isAuto() {
		return false;
	}

	/**
	 * 没有输入管脚
	 */
	public void input(Pin... pins) {
	}

	/**
	 * 没有输出管脚
	 */
	public Pin output(int pinIdx) {
		return null;
	}

	public String getPackageKey() {
		return this._progress.getPackageKey();
	}

	public String getEventKey() {
		return this._progress.getProgressKey() + END_SUFFIX;
	}
}
