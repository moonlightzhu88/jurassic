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
 * 任务的基类
 * 每一个业务流程由若干个任务组成
 * 每个任务代表着一小段业务逻辑
 * 
 * @author yzhu
 */
public abstract class Task extends Event {
	protected String _desc;// task在流程中的显示名称
	// 任务之间通过前驱和后继相互联系
	protected List<Task> _prevTasks = new ArrayList<>();// 前驱任务
	protected List<Task> _nextTasks = new ArrayList<>();// 后继任务
	protected Progress _progress;// 任务所属的流程
	// 任务执行的截止时间,-1表示任务没有截止时间
	// 每个任务可以设置一个截止完成时间,在规定时间内无法完成流程的
	// 则中断流程执行,以超时错误结束
	protected long _cutoffTime = -1;
	// 而无需交由任意的handler处理，多用在流程中一些条件判断等环节上

	public Task(Progress progress, String desc) {
		this._progress = progress;
		this._desc = desc;
		if (progress != null)
			progress.addTask(this);// 每次创建task的时候,将其绑定到对应的链上
	}

	/**
	 * 判断是否是自动任务
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
	 * 将next任务连接到当前任务后续执行
	 */
	public Task bind(Task nextTask) {
		this._nextTasks.add(nextTask);
		nextTask._prevTasks.add(this);

		return nextTask;
	}

	protected Map<Task, Express> _conditions = new HashMap<>();// 跳转路径上的条件表达式
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
	 * 判断当前任务是否可以执行
	 * 可以执行的标准是：当前任务的所有前驱任务都以结束
	 */
	public boolean isReady() {
		// 当前任务的状态为初始
		if (this._status != 0)
			return false;

		// 所有前驱任务的状态都必须是完成
		for (Task prev : this._prevTasks) {
			if (!prev.isAuto() && prev._status != S_END) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 判断task任务是否是当前任务的下属（包含自身）任务
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
	 * 绑定输入管脚数据
	 */
	public abstract void input(Pin... pins);

	/**
	 * 获得指定位置的输出管脚数据
	 */
	public abstract Pin output(int pinIdx);

}
