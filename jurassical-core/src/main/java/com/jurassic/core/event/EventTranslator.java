package com.jurassic.core.event;

import com.lmax.disruptor.EventTranslatorOneArg;

/**
 * �¼�������
 * �����¼�����Ϣ����copy�����������¼�������
 * 
 * @author yzhu
 */
public class EventTranslator<T extends Event> implements
		EventTranslatorOneArg<EventWrapper<T>, T> {

	/**
	 * ����Ҫ������¼������sequenceָ��λ�õĻ�����
	 */
	public void translateTo(EventWrapper<T> wrapper, long sequence, T event) {
		wrapper.setEvent(event);
	}

}
