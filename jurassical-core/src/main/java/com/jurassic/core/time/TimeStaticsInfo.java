package com.jurassic.core.time;

import com.jurassic.core.event.Event;

import java.util.List;

/**
 * 任务执行的时间统计
 * 收集了各个处理器在一个固定时间段中执行任务的情况
 * 包括处理的任务数量,等待的任务数据时间等信息
 * 
 * @author yzhu
 */
public class TimeStaticsInfo {

	// 完成事件数量
	private int _finishNum;
	// 发生错误数量
	private int _errNum;
	// 事件平均等待执行时间,毫秒
	private long _avgWaitingTime;
	// 事件平均执行时间,毫秒
	private long _avgExecTime;

	/**
	 * 完成时间的执行信息统计
	 */
	public void finishEvent(List<Event> events) {
		long waitingTime = 0;
		long executeTime = 0;
		for (Event t : events) {
			// 指令等待的时间
			waitingTime += t.getDealTime() - t.getPublishTime();
			// 指令执行的时间
			executeTime += t.getFinishTime() - t.getDealTime();

			this._finishNum++;
			if (t.getError() != null)
				this._errNum++;
		}

		this._avgWaitingTime += (waitingTime - this._avgWaitingTime * events.size())
				/ this._finishNum;
		this._avgExecTime += (executeTime - this._avgExecTime * events.size())
				/ this._finishNum;

	}

	/**
	 * 重置统计信息
	 */
	public void reset() {
		this._avgExecTime = 0;
		this._avgWaitingTime = 0;
		this._errNum = 0;
		this._finishNum = 0;
	}

	public int getFinishNum() {
		return _finishNum;
	}

	public int getErrNum() {
		return _errNum;
	}

	public long getAvgWaitingTime() {
		return _avgWaitingTime;
	}

	public long getAvgExecTime() {
		return _avgExecTime;
	}

}
