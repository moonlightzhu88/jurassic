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
 * ��һ���
 * �����̳߳صķ�ʽ�����¼�
 * 
 * @author yzhu
 * 
 * @param <T>
 */
public class SingleEventProcessor<T extends Event> extends
		AbstractEventProcessor<T> {

	private final WorkHandler<EventWrapper<T>>[] _workers;// �������Ĺ����ߣ����������߼�����
	// �����ߵ������ʹ����ǰ���߳�������Ӧ��һ�������߶�Ӧһ���߳�

	public SingleEventProcessor(
			AbstractHandler handler, WorkHandler<EventWrapper<T>>[] workers, int power) {
		super(handler);
		this._workers = workers;
		for (WorkHandler<EventWrapper<T>> worker : workers) {
			// ���ù����ߣ�Ϊÿ��������ָ�������߼�
			worker.setHandler(handler);
		}
		// ��ʼ���������
		if (power > Constant.DRPT_MAX_DATA_SIZE_POWER) {
			power = Constant.DRPT_MAX_DATA_SIZE_POWER;
		}
		if (power > 0) {
			this._ringBufferSize = (1 << power);
		}
	}

	protected Disruptor<EventWrapper<T>> initProcessor(ExecutorService es) {
		// ����disruptor������Worker
		Disruptor<EventWrapper<T>> disruptor = new Disruptor<>(
				new EventFactory<>(), this._ringBufferSize, es);
		disruptor.handleEventsWithWorkerPool(this._workers);
		return disruptor;
	}

}
