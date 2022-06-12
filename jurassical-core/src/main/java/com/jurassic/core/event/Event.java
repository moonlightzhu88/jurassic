package com.jurassic.core.event;

/**
 * 事件的基本定义
 * 
 * @author yzhu
 */
public abstract class Event {

	public Event() {
	}

	/**
	 * 获得事件所在的包
	 */
	public abstract String getPackageKey();

	/**
	 * 获得事件的类型
	 */
	public abstract String getEventKey();

	public static final int S_START = 1;// 状态-开始运行
	public static final int S_RUN = 2;// 状态-执行中
	public static final int S_END = 3;// 状态-执行完成
	public static final int S_HALT = 4;// 状态-暂停
	public static final int S_RESUME = 5;// 状态-恢复执行
	protected int _status = 0;// 状态

	public int getStatus() {
		return this._status;
	}

	/**
	 * 重置事件的各个状态信息
	 */
	public void reset() {
		this._status = 0;
		this._dealTime = -1;
		this._publishTime = -1;
		this._finishTime = -1;
		this._ex = null;
	}

	/**
	 * 事件发布
	 */
	public void publish() {
		if (this._status == 0) {
			this._status = S_START;
			this.setPublishTime(System.currentTimeMillis());
			if (this._monitor != null) {
				// 如果该事件具有监控，标记事件开始运行
				this._monitor.eventStart(this);
			}
		}
	}

	/**
	 * 事件处理
	 */
	public void run() {
		if (this._status == S_START) {
			this.setDealTime(System.currentTimeMillis());
			this._status = S_RUN;
		}
	}

	/**
	 * 事件暂停处理
	 */
	public void halt() {
		if (this._status == S_RUN) {
			this._status = S_HALT;
		}
	}

	/**
	 * 事件准备继续执行
	 */
	public void startResume() {
		if (this._status == S_HALT) {
			this._status = S_RESUME;
		}
	}

	/**
	 * 判断时间是否具备可以继续执行的状态
	 */
	public boolean isResumed() {
		return this._status == S_RESUME;
	}

	/**
	 * 事件继续运行
	 */
	public void resume() {
		if (this._status == S_RESUME) {
			this._status = S_RUN;
		}
	}

	/**
	 * 事件处理完成
	 */
	public void end(Throwable ex) {
		if (this._status == S_RUN) {
			this.setFinishTime(System.currentTimeMillis());
			this._status = S_END;
			this._ex = ex;
			if (this._monitor != null) {
				// 如果该事件具有监控，标记事件结束运行
				this._monitor.eventEnd(this);
			}
		}
	}

	// 时间生命周期中的各个时间点
	private long _publishTime = -1;// 发布时间
	private long _dealTime = -1;// 处理时间
	private long _finishTime = -1;// 完成时间
	private Throwable _ex;// 事件处理错误

	/**
	 * 判断时间是否处理出错
	 */
	public Throwable getError() {
		return this._ex;
	}

	public long getPublishTime() {
		return _publishTime;
	}

	private void setPublishTime(long publishTime) {
		if (this._publishTime == -1)
			this._publishTime = publishTime;
	}

	public long getDealTime() {
		return _dealTime;
	}

	private void setDealTime(long dealTime) {
		if (this._dealTime ==-1)
			this._dealTime = dealTime;
	}

	public long getFinishTime() {
		return _finishTime;
	}

	private void setFinishTime(long finishTime) {
		if (this._finishTime == -1)
			this._finishTime = finishTime;
	}

	// 调度属性
	public static final int SINGLE = 0;// 单次执行
	public static final int RETRY_ON_ERROR = 1;// 错误的时候重试
	public static final int MULTIPLE = 2;// 多次执行

	private int _scheduleType = SINGLE;// 调度类型
	private long _scheduleSpan = 0;// 执行间隔，ms
	private int _retryNum = 0;// 重新尝试次数，不包括第一次执行

	public void setScheduleType(int type) {
		this._scheduleType = type;
	}

	public int getScheduleType() {
		return this._scheduleType;
	}

	public void setScheduleSpan(long span) {
		this._scheduleSpan = span;
	}

	public long getScheduleSpan() {
		return this._scheduleSpan;
	}

	public void setRetryNum(int num) {
		this._retryNum = num;
	}

	public int getRetryNum() {
		return this._retryNum;
	}

	private EventMonitor _monitor;// 事件的监控器

	public void setMonitor(EventMonitor monitor) {
		this._monitor = monitor;
	}
}
