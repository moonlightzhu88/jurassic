package com.jurassic.core.processor.impl;

import java.util.concurrent.ExecutorService;

import com.jurassic.core.bus.Constant;
import com.jurassic.core.event.Event;
import com.jurassic.core.event.EventFactory;
import com.jurassic.core.event.EventWrapper;
import com.jurassic.core.handler.AbstractHandler;
import com.jurassic.core.processor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * (批量)处理器
 * 采用单一线程批量操作的方式
 * 
 * @author yzhu
 * 
 * @param <T>
 */
public class BatchEventProcessor<T extends Event> extends
		AbstractEventProcessor<T> {

	// 工作者
	private final EventHandler<EventWrapper<T>> _worker;

	public BatchEventProcessor(AbstractHandler handler,
							   EventHandler<EventWrapper<T>> worker, int power) {
		super(handler);
		this._worker = worker;
		// 为工作者指定处理逻辑
		this._worker.setHandler(handler);
		// 初始化处理队列
		if (power > Constant.DRPT_MAX_DATA_SIZE_POWER) {
			power = Constant.DRPT_MAX_DATA_SIZE_POWER;
		}
		if (power > 0) {
			this._ringBufferSize = (1 << power);
		}
	}

	@SuppressWarnings("unchecked")
	protected Disruptor<EventWrapper<T>> initProcessor(ExecutorService es) {
		Disruptor<EventWrapper<T>> disruptor = new Disruptor<>(
				new EventFactory<>(), this._ringBufferSize, es);
		disruptor.handleEventsWith(this._worker);
		return disruptor;
	}
}
