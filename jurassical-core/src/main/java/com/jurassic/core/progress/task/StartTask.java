package com.jurassic.core.progress.task;

import com.jurassic.core.progress.handler.pin.Pin;
import com.jurassic.core.progress.Progress;

/**
 * 流程的启动任务
 * name和key属性都采用流程名称+_start的形式
 * 用来区分各个不同流程的开始任务
 *
 * @author yzhu
 */
public class StartTask extends Task {
	// 各个业务流程有各自不同的Start节点,他们的key以_start结尾,以整个业务流程的key作为开头
	public static final String START_SUFFIX = "_start";

	public StartTask(Progress progress) {
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
		return this._progress.getProgressKey() + START_SUFFIX;
	}
}
