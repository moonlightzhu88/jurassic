package com.jurassic.core.event;

/**
 * �¼�ִ�еķ�װ��
 * ���������¼��������е���Ԫ������
 * 
 * @author yzhu
 */
public class EventWrapper<T extends Event> {

	private T _event;// ��װ���¼�

	/**
	 * �����Ҫ�����ʵ���¼�
	 */
	public T getEvent() {
		return _event;
	}

	/**
	 * ������Ҫ�����ʵ���¼�
	 */
	public void setEvent(T event) {
		this._event = event;
	}

	/**
	 * ���û������¼�
	 */
	public void reset() {
		this._event = null;
	}
}
