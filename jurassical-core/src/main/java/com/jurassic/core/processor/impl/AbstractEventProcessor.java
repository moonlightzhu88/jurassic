package com.jurassic.core.processor.impl;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.LockSupport;

import com.jurassic.core.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jurassic.core.bus.Constant;
import com.jurassic.core.processor.EventProcessor;
import com.jurassic.core.event.Event;
import com.jurassic.core.event.FireDelayEvent;
import com.jurassic.core.event.EventTranslator;
import com.jurassic.core.event.EventWrapper;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * EventProcessorʵ�ֻ���
 * ����disruptor���߳�ģ��
 *
 * @author yzhu
 * 
 */
public abstract class AbstractEventProcessor<T extends Event> implements
		EventProcessor<T> {
	// disruptor���������������С
	protected int _ringBufferSize = (1 << Constant.DRPT_DATA_SIZE_POWER);
	// ������־
	protected volatile boolean _started = false;
	// ��������
	protected final AbstractHandler _handler;
	// �ڲ�������disruptor
	protected Disruptor<EventWrapper<T>> _disruptor;
	// �¼���������
	protected EventTranslator<T> _translator = new EventTranslator<>();
	// �ӳ�ִ�е��¼�����,�ȴ����а���FIFO�Ĳ��Դ��������ʱ�޷��õ�������¼�
	protected Queue<T> _delayQueue = new ConcurrentLinkedQueue<>();

	protected static Logger logger = LoggerFactory.getLogger(AbstractEventProcessor.class);

	public AbstractEventProcessor(AbstractHandler handler)  {
		this._handler = handler;
	}

	public AbstractHandler getHandler() {
		return this._handler;
	}

	/**
	 * ��ʼ��������
	 */
	protected abstract Disruptor<EventWrapper<T>> initProcessor(ExecutorService es);

	public synchronized void shutdown() {
		if (!this._started) {
			return;
		}
		// �رմ�����,�ܾ������µ��¼�
		this._started = false;
		// ����ȴ���������Ȼ��δ����ĵ��¼�
		// �����ȴ�����Щ�¼�
		while (!this._delayQueue.isEmpty()) {
			T event = this._delayQueue.poll();
			// ���Խ����е�delay�¼����뵽�������Ĵ��������
			while (!this._disruptor.getRingBuffer().tryPublishEvent(
					this._translator, event)) {
				LockSupport.parkNanos(1000000L);
			}
		}
		// �ȴ����еĴ�������running��event�������
		while (this.hasDealingEvent()) {
			LockSupport.parkNanos(1000000L);
		}
		// �ر�disruptor
		this._disruptor.shutdown();
		logger.info("processor[" + this.getHandler().getHandlerKey() + "] shutdown");
	}

	public boolean doEvent(T event) {
		// һ������ر�,���������µ��¼�
		if (!this._started) {
			return false;
		}
		if (event instanceof FireDelayEvent) {
			// ����ʱ���¼������Խ�delayQueue�е��¼����뵽�������Ĵ��������
			if (!this._delayQueue.isEmpty()) {
				// �ӳٶ��в��յĻ�,������Щ����ɳ����������¼�
				while (!this._delayQueue.isEmpty()) {
					T delay = this._delayQueue.peek();
					// ���Դ����ӳ��¼���
					boolean success = this._disruptor.getRingBuffer()
								.tryPublishEvent(this._translator, delay);
					if (success) {
						// �����ɹ�,���¼���delay����������,ͬʱ������һ���ӳ��¼�
						this._delayQueue.poll();
					} else {
						// û�з����ɹ�,��delay�¼�������ȴ���һ��fire
						// ��Ϊ��FIFOԭ��,��˺������¼������ж�
						break;
					}
				}
			}
		} else {
			// �����������¼�
			// ��¼ָ��ķ���ʱ��
			event.publish();
			if (this._delayQueue.isEmpty()) {
				// ���û��delay�¼�,��ֱ�ӳ��Է����¼�
				boolean success = this._disruptor.getRingBuffer().tryPublishEvent(
						this._translator, event);
				if (!success) {
					// ��������������������뵽delayQueue
					this._delayQueue.add(event);
				}
			} else {
				// �����delay�¼�������FIFO��ԭ�򣬽�����뵽delayQueue�����
				this._delayQueue.add(event);
			}
		}
		return true;
	}

	public synchronized boolean start(ExecutorService es) {
		if (this._started)
			return true;

		// ��ʼ��processor
		this._disruptor = this.initProcessor(es);
		if (this._disruptor == null) {
			// ��ʼ��ʧ��
			this._started = false;
			logger.warn("processor[" + this.getHandler().getHandlerKey() + "] start fail");
			return false;
		}
		// ����processor
		if (this._disruptor.start() != null) {
			// �������
			this._started = true;
			logger.info("processor[" + this.getHandler().getHandlerKey() + "] start success");
			return true;
		} else {
			// ����ʧ��
			this._started = false;
			this._disruptor = null;
			logger.warn("processor[" + this.getHandler().getHandlerKey() + "] start fail");
			return false;
		}
	}

	public boolean isStarted() {
		return this._started;
	}

	public boolean hasDealingEvent() {
		// û�����е�����ǲ����д����¼���
		if (!this._started)
			return false;
		// delayQuere�����¼���˵����������Ȼ��Ҫ�����¼�
		if (!this._delayQueue.isEmpty())
			return true;
		// ��鴦��������Ƿ���δ������¼�
		long min = this._disruptor.getRingBuffer().getMinimumGatingSequence();
		long curr = this._disruptor.getRingBuffer().getCursor();
		for (long i = min; i <= curr; i++) {
			EventWrapper<?> wrapper = this._disruptor.getRingBuffer().get(i);
			// ÿһ����������е��¼����������wrapper��reset�����event
			if (wrapper.getEvent() != null)
				return true;
		}
		return false;
	}

}
