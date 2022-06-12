package com.jurassic.core.progress.handler.impl;

import com.jurassic.core.bus.impl.EBus;
import com.jurassic.core.progress.handler.BatchTaskHandler;
import com.jurassic.core.progress.task.EndTask;

import java.util.List;

/**
 * ���̵Ľ���
 * 
 * @author yzhu
 * 
 */
public class EndHandler extends BatchTaskHandler<EndTask> {

	public EndHandler() {
		super();
	}

	public String getHandlerKey() {
		return EBus.COMPONENT_KEY_END;
	}

	public void handle(List<EndTask> tasks) throws Throwable {
		for (EndTask task : tasks) {
			// ���̽�������
			task.getProgress().end();
		}
	}
}
