package com.jurassic.core.event;

/**
 * lmax���¼�����
 * ����Ϊÿ�����������¼���������ʼ���¼�����
 * 
 * @author yzhu
 */
public class EventFactory<T extends Event> implements
		com.lmax.disruptor.EventFactory<EventWrapper<T>> {

	public EventWrapper<T> newInstance() {
		return new EventWrapper<>();
	}

}
