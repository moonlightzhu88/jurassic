package com.jurassic.core.progress;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.jurassic.core.progress.handler.pin.Pin;
import com.jurassic.core.notification.ResultNotification;
import com.jurassic.core.progress.task.EndTask;
import com.jurassic.core.progress.task.StartTask;
import com.jurassic.core.progress.task.Task;


/**
 * 业务流程定义
 * 流程由任意多个任务前后相连组成
 * 构成业务的完整逻辑
 * 流程始于start任务,结束于end任务
 *
 * @author yzhu
 */
public class Progress {
	public final static int S_INIT = 0;// 初始状态,流程刚刚创建
	public final static int S_RUN = 1;// 流程执行start任务,开始执行
	public final static int S_INTERRUPTED = 2;// 流程执行过程中,在某些任务上发生错误导致中断结束
	public final static int S_END = 3;// 流程正常结束
	private int _status;// 流程的状态

	// 流程的任务信息
	private final String _packageKey;// 流程所属组件包的key
	private final String _progressKey;// 业务流程的类型
	private final List<Task> _tasks = new ArrayList<>();// 储存所有的任务
	private final StartTask _startTask;// 开始任务
	private final EndTask _endTask;// 结束任务

	// 每个流程可以选择性的设置业务的结束点
	// 业务结束点是指可以立即获得业务过程输出数据的任务点
	// 流程执行到结束点后触发通知操作,将业务输出结果异步通知给请求方
	private Task _endPoint;// 触发notification的任务
	private ResultNotification _notification;// 回调接口
	private Pin _result;// 获取流程结果的接口

	// 流程的异常处理配置
	// 异常捕获表,每个条目分为3个元素:
	// 前2项任务说明了异常捕获的任务范围,第三项指定了异常处理的入口任务
	private final List<Task[]> _catchTbl;

	private final AtomicInteger _runningNum;// 流程的运行计数器
	private Progress _parent;// 父流程

	private ProgressMonitor _monitor;// 流程的监控器

	public Progress(String packageKey, String progressKey, AtomicInteger runningNum) {
		this._packageKey = packageKey;
		this._progressKey = progressKey;
		this._runningNum = runningNum;
		this._status = S_INIT;
		this._catchTbl = new ArrayList<>();
		// 初始化构建流程的时候自动构建流程的起始和结束任务
		this._startTask = new StartTask(this);
		this._endTask = new EndTask(this);
	}

	public void setMonitor(ProgressMonitor monitor) {
		this._monitor = monitor;
	}

	public void setParent(Progress parent){
		this._parent = parent;
	}
	
	public Progress getParent(){
		return this._parent;
	}

	/**
	 * 根据发生错误的任务找到与之对应的异常处理配置
	 */
	public Task findErrorEntry(Task task) {
		for (Task[] entry : this._catchTbl) {
			// 如果发生错误的任务在捕获范围之间,则返回异常处理入口任务
			// 每一个catch entry定义了检测异常错误的范围，包括任务的上界,下界和异常处理入口任务
			if (entry[0].hasChild(task) && task.hasChild(entry[1])) {
				return entry[2];
			}
		}
		return null;
	}

	/**
	 * 添加流程的异常处理过程
	 */
	public void addCatchTable(Task start, Task end, Task errorEntry) {
		if (start == null || end == null || errorEntry == null)
			return;
		_catchTbl.add(new Task[] { start, end, errorEntry });
	}

	public String getPackageKey() {
		return this._packageKey;
	}

	/**
	 * 获得流程的输出数据
	 */
	protected Object getResult() {
		return this._result != null ?
				this._result.getData() : null;
	}

	/**
	 * 配置流程的输出
	 * 流程的输出点表示当流程执行到对应任务的时候，可以获得流程的结果数据并返回
	 */
	public void setEndpoint(Task endPoint, Pin result) {
		// 设置回调点必须有触发的task和对应属性
		if (endPoint == null)
			return;
		this._endPoint = endPoint;
		this._result = result;
	}

	/**
	 * 设置流程的输出接口
	 */
	public void setNotification(ResultNotification notification) {
		this._notification = notification;
	}

	/**
	 * 通知流程输出结果
	 */
	public void notifyResult() {
		if (this._notification == null)
			return;
		// 获得输出
		Object result = this.getResult();
		this._notification.notify(result);
		// 一个流程只通知一次输出结果
		this._notification = null;
	}

	/**
	 * 通知流程异常
	 */
	public void notifyError(Throwable ex) {
		if (this._notification == null)
			return;
		// 获得输出
		this._notification.notify(ex);
		// 一个流程只通知一次输出结果
		this._notification = null;
	}

	public Task getEndpoint() {
		return this._endPoint;
	}

	/**
	 * 业务流程启动
	 * 通常在start任务处理的时候执行
	 */
	public void run() {
		this._status = S_RUN;
		// 更新流程运行数量的统计信息
		if (this._runningNum != null) {
			this._runningNum.incrementAndGet();
		}
		// 监控流程的开始
		if (this._monitor != null) {
			this._monitor.serviceStart(this);
		}
	}

	/**
	 * 流程发送异常中断了
	 */
	public void happenException() {
		this._status = S_INTERRUPTED;
	}

	/**
	 * 业务流程结束
	 * 通常在end任务处理的时候执行
	 */
	public void end() {
		// 对于正常结束的流程标记状态为end,否则为interrupted结束
		if (this._status != S_INTERRUPTED)
			this._status = S_END;
		// 结束的同时更新流程运行数量的统计信息
		if (this._runningNum != null) {
			this._runningNum.decrementAndGet();
		}
		// 监控流程的结束
		if (this._monitor != null) {
			this._monitor.serviceEnd(this);
		}
	}

	/**
	 * 判断一个业务流程是否已经中断了
	 */
	public boolean isInterrupted() {
		return this._status == S_INTERRUPTED;
	}

	/**
	 * 设置TaskChain任务链中所有任务的执行截止时间
	 */
	public void setCutOffTime(long time) {
		for (Task t : this._tasks) {
			t.setCutOffTime(time);
		}
	}

	public String getProgressKey() {
		return _progressKey;
	}

	/**
	 * 新建任务的时候，将新建任务添加到链中
	 */
	public void addTask(Task task) {
		this._tasks.add(task);
	}

	public Task getEndTask() {
		return this._endTask;
	}

	public Task getStartTask() {
		return this._startTask;
	}

}
