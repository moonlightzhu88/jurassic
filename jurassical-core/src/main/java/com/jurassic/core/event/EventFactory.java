package com.jurassic.core.event;

/**
 * lmax的事件工厂
 * 负责为每个处理器的事件缓冲区初始化事件对象
 * 
 * @author yzhu
 */
public class EventFactory<T extends Event> implements
		com.lmax.disruptor.EventFactory<EventWrapper<T>> {

	public EventWrapper<T> newInstance() {
		return new EventWrapper<>();
	}

}
