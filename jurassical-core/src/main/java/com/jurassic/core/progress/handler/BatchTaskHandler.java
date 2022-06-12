package com.jurassic.core.progress.handler;

import java.util.List;

import com.jurassic.core.bus.Constant;
import com.jurassic.core.handler.AbstractHandler;
import com.jurassic.core.progress.task.Task;

/**
 * ҵ��(����)����ӿ�
 * �ô�����һ�δ���һ������
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
	 * ҵ����
	 */
	public abstract void handle(List<T> tasks) throws Throwable;
	

}
