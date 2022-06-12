package com.jurassic.core.progress.handler;

import com.jurassic.core.bus.Constant;
import com.jurassic.core.handler.AbstractHandler;
import com.jurassic.core.progress.task.Task;

/**
 * ҵ����Ļ���ӿ�
 * �ô�����һ�δ���һ������
 * 
 * @author yzhu
 *
 */
public abstract class TaskHandler<T extends Task> extends AbstractHandler {

	public TaskHandler() {
		super();
		this._numOfThread = Constant.DRPT_WORKER_SIZE;
		this._powerOfBuffer = Constant.DRPT_DATA_SIZE_POWER;
	}

	/**
	 * ҵ����
	 */
	public abstract void handle(T task) throws Throwable;

}
