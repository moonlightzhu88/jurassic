package com.jurassic.core.event;

import com.lmax.disruptor.EventTranslatorOneArg;

/**
 * 事件传输类
 * 负责将事件的信息数据copy到处理器的事件队列中
 * 
 * @author yzhu
 */
public class EventTranslator<T extends Event> implements
		EventTranslatorOneArg<EventWrapper<T>, T> {

	/**
	 * 将需要处理的事件存放入sequence指定位置的缓冲区
	 */
	public void translateTo(EventWrapper<T> wrapper, long sequence, T event) {
		wrapper.setEvent(event);
	}

}
