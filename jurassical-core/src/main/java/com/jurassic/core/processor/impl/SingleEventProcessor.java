package com.jurassic.core.processor.impl;

import java.util.concurrent.ExecutorService;

import com.jurassic.core.bus.Constant;
import com.jurassic.core.event.Event;
import com.jurassic.core.event.EventFactory;
import com.jurassic.core.event.EventWrapper;
import com.jurassic.core.handler.AbstractHandler;
import com.jurassic.core.processor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * 单一组件
 * 采用线程池的方式处理事件
 * 
 * @author yzhu
 * 
 * @param <T>
 */
public class SingleEventProcessor<T extends Event> extends
		AbstractEventProcessor<T> {

	private final WorkHandler<EventWrapper<T>>[] _workers;// 处理器的工作者，负责具体的逻辑运行
	// 工作者的数量和处理的前的线程数量对应，一个工作者对应一个线程

	public SingleEventProcessor(
			AbstractHandler handler, WorkHandler<EventWrapper<T>>[] workers, int power) {
		super(handler);
		this._workers = workers;
		for (WorkHandler<EventWrapper<T>> worker : workers) {
			// 设置工作者，为每个工作者指定处理逻辑
			worker.setHandler(handler);
		}
		// 初始化处理队列
		if (power > Constant.DRPT_MAX_DATA_SIZE_POWER) {
			power = Constant.DRPT_MAX_DATA_SIZE_POWER;
		}
		if (power > 0) {
			this._ringBufferSize = (1 << power);
		}
	}

	protected Disruptor<EventWrapper<T>> initProcessor(ExecutorService es) {
		// 构建disruptor，设置Worker
		Disruptor<EventWrapper<T>> disruptor = new Disruptor<>(
				new EventFactory<>(), this._ringBufferSize, es);
		disruptor.handleEventsWithWorkerPool(this._workers);
		return disruptor;
	}

}
