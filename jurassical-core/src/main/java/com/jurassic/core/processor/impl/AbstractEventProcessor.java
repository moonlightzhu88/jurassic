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
 * EventProcessor实现基类
 * 采用disruptor的线程模型
 *
 * @author yzhu
 * 
 */
public abstract class AbstractEventProcessor<T extends Event> implements
		EventProcessor<T> {
	// disruptor的数据请求区域大小
	protected int _ringBufferSize = (1 << Constant.DRPT_DATA_SIZE_POWER);
	// 启动标志
	protected volatile boolean _started = false;
	// 处理器的
	protected final AbstractHandler _handler;
	// 内部处理器disruptor
	protected Disruptor<EventWrapper<T>> _disruptor;
	// 事件对象翻译器
	protected EventTranslator<T> _translator = new EventTranslator<>();
	// 延迟执行的事件队列,等待队列按照FIFO的策略存放所有暂时无法得到处理的事件
	protected Queue<T> _delayQueue = new ConcurrentLinkedQueue<>();

	protected static Logger logger = LoggerFactory.getLogger(AbstractEventProcessor.class);

	public AbstractEventProcessor(AbstractHandler handler)  {
		this._handler = handler;
	}

	public AbstractHandler getHandler() {
		return this._handler;
	}

	/**
	 * 初始化处理器
	 */
	protected abstract Disruptor<EventWrapper<T>> initProcessor(ExecutorService es);

	public synchronized void shutdown() {
		if (!this._started) {
			return;
		}
		// 关闭处理器,拒绝接受新的事件
		this._started = false;
		// 如果等待队列中仍然有未处理的的事件
		// 则优先处理这些事件
		while (!this._delayQueue.isEmpty()) {
			T event = this._delayQueue.poll();
			// 尝试将所有的delay事件加入到处理器的处理队列中
			while (!this._disruptor.getRingBuffer().tryPublishEvent(
					this._translator, event)) {
				LockSupport.parkNanos(1000000L);
			}
		}
		// 等待所有的处理器把running的event处理完成
		while (this.hasDealingEvent()) {
			LockSupport.parkNanos(1000000L);
		}
		// 关闭disruptor
		this._disruptor.shutdown();
		logger.info("processor[" + this.getHandler().getHandlerKey() + "] shutdown");
	}

	public boolean doEvent(T event) {
		// 一旦组件关闭,将不接受新的事件
		if (!this._started) {
			return false;
		}
		if (event instanceof FireDelayEvent) {
			// 遇到时钟事件，尝试将delayQueue中的事件加入到处理器的处理队列中
			if (!this._delayQueue.isEmpty()) {
				// 延迟队列不空的话,处理那些到达可出发条件的事件
				while (!this._delayQueue.isEmpty()) {
					T delay = this._delayQueue.peek();
					// 可以触发延迟事件了
					boolean success = this._disruptor.getRingBuffer()
								.tryPublishEvent(this._translator, delay);
					if (success) {
						// 发布成功,将事件从delay队列中拿走,同时考察下一条延迟事件
						this._delayQueue.poll();
					} else {
						// 没有发布成功,则delay事件不处理等待下一次fire
						// 因为是FIFO原则,因此后续的事件无需判断
						break;
					}
				}
			}
		} else {
			// 处理正常的事件
			// 记录指令的发布时间
			event.publish();
			if (this._delayQueue.isEmpty()) {
				// 如果没有delay事件,则直接尝试发布事件
				boolean success = this._disruptor.getRingBuffer().tryPublishEvent(
						this._translator, event);
				if (!success) {
					// 如果处理队列已满，则加入到delayQueue
					this._delayQueue.add(event);
				}
			} else {
				// 如果有delay事件，则按照FIFO的原则，将其加入到delayQueue的最后
				this._delayQueue.add(event);
			}
		}
		return true;
	}

	public synchronized boolean start(ExecutorService es) {
		if (this._started)
			return true;

		// 初始化processor
		this._disruptor = this.initProcessor(es);
		if (this._disruptor == null) {
			// 初始化失败
			this._started = false;
			logger.warn("processor[" + this.getHandler().getHandlerKey() + "] start fail");
			return false;
		}
		// 启动processor
		if (this._disruptor.start() != null) {
			// 启动完成
			this._started = true;
			logger.info("processor[" + this.getHandler().getHandlerKey() + "] start success");
			return true;
		} else {
			// 启动失败
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
		// 没有运行的组件是不含有处理事件的
		if (!this._started)
			return false;
		// delayQuere中有事件，说明处理器仍然需要处理事件
		if (!this._delayQueue.isEmpty())
			return true;
		// 检查处理队列中是否还有未处理的事件
		long min = this._disruptor.getRingBuffer().getMinimumGatingSequence();
		long curr = this._disruptor.getRingBuffer().getCursor();
		for (long i = min; i <= curr; i++) {
			EventWrapper<?> wrapper = this._disruptor.getRingBuffer().get(i);
			// 每一个处理队列中的事件被处理完后wrapper会reset里面的event
			if (wrapper.getEvent() != null)
				return true;
		}
		return false;
	}

}
