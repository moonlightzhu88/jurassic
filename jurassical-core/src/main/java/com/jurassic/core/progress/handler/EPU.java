package com.jurassic.core.progress.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.jurassic.core.bus.Constant;
import com.jurassic.core.handler.AbstractHandler;
import com.jurassic.core.progress.Progress;
import com.jurassic.core.progress.task.Task;

/**
 * 中央处理器
 * 主要负责事件的运算逻辑
 * 当每一个事件处理完毕后,由中央处理器计算后续需要执行的事件
 * 每一个部署的应用包原则上需要提供唯一的中央处理器
 * 
 * @author yzhu
 */
public class EPU extends AbstractHandler {

	public EPU() {
		super();
		this._numOfThread = 1;
		this._powerOfBuffer = Constant.DEFAULT_EPU_POWER;
	}

	public String getHandlerKey() {
		return "epu";
	}

	/**
	 * 计算流程的下一步任务
	 */
	public List<Task> calculate(Task currentTask) {
		Throwable error = currentTask.getError();
		if (currentTask.getProgress().isInterrupted()
				&& currentTask != currentTask.getProgress().getEndTask()) {
			// 如果整个任务流程已经中断,则忽略该任务,不再继续执行
			// 流程的end task一定要被执行
			return null;
		}

		List<Task> nextTasks = new ArrayList<>();
		if (error != null) {
			// 通过异常处理表计算流程的下一步任务
			nextTasks.add(this.interrupt(currentTask, error));
			return nextTasks;
		}

		boolean outOfTime = currentTask.getCutoffTime() != -1
				&& System.currentTimeMillis() > currentTask.getCutoffTime();
		// 检查任务执行时间,如果超过截至时间,则任务被标记为超时,后续的任务将不被执行且全部标记为cancel
		if (outOfTime) {
			// 执行超时,则抛出timeout异常,终止后续任务执行,并返回客户端该异常
			nextTasks.add(this.interrupt(currentTask, new TimeoutException("timeout")));
			return nextTasks;
		}

		if (currentTask == currentTask.getProgress().getEndpoint()) {
			// 这里代表流程实际上走完了，可以输出结果信息了
			currentTask.getProgress().notifyResult();
		}

		// 正常完成前提下,计算流程的下一步
		List<Task> tasks = currentTask.getNextTasks();
		if (tasks != null) {
			for (Task next : tasks) {
				if (next.isReady()) {
					// 如果下一步任务以准备好，则可以加入执行
					nextTasks.add(next);
				}
			}
		}
		return nextTasks;
	}

	/**
	 * 计算流程在发生中断的时候，应该执行的任务
	 */
	protected Task interrupt(Task interruptedTask, Throwable ex) {

		Progress progress = interruptedTask.getProgress();
		// 流程到此中断,后续不在处理任何该流程下的task计算,参照calculate方法的开始判断逻辑
		progress.happenException();
		// 通知异常错误
		progress.notifyError(ex);
		// 查找是否有匹配的catchTable
		Task entry = progress.findErrorEntry(interruptedTask);
		if (entry == null) {
			// 如果没有匹配成功,默认下一个task为end task
			entry = progress.getEndTask();
		}
		return entry;
	}

}
