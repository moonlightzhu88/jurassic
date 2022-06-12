package com.jurassic.core.progress.handler;

import java.util.List;

import com.jurassic.core.bus.Constant;
import com.jurassic.core.handler.AbstractHandler;
import com.jurassic.core.progress.task.Task;

/**
 * 业务(批量)处理接口
 * 该处理器一次处理一批任务
 * 
 * @author yzhu
 */
public abstract class BatchTaskHandler<T extends Task> extends AbstractHandler {

	public BatchTaskHandler() {
		super();
		this._numOfThread = 1;
		this._powerOfBuffer = Constant.DRPT_DATA_SIZE_POWER;
	}

	/**
	 * 业务处理
	 */
	public abstract void handle(List<T> tasks) throws Throwable;
	

}
