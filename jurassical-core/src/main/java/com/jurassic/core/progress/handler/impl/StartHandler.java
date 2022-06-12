package com.jurassic.core.progress.handler.impl;

import com.jurassic.core.bus.impl.EBus;
import com.jurassic.core.progress.handler.BatchTaskHandler;
import com.jurassic.core.progress.task.StartTask;

import java.util.List;

/**
 * ���̵Ŀ�ʼ
 * 
 * @author yzhu
 * 
 */
public class StartHandler extends BatchTaskHandler<StartTask> {

	public StartHandler() {
		super();
	}

	public String getHandlerKey() {
		return EBus.COMPONENT_KEY_START;
	}

	public void handle(List<StartTask> tasks) throws Throwable {
		for (StartTask task : tasks) {
			// ���̿�ʼִ��
			task.getProgress().run();
		}
	}
}
