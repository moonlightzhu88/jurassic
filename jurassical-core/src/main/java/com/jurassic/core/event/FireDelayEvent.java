package com.jurassic.core.event;

/**
 * �����ӳ��¼�ִ��
 * �������������������
 * 
 * @author yzhu
 *
 */
public class FireDelayEvent extends Event {
	
	public static final String KEY = "timer";
	
	// ������singletonʵ��
	public static FireDelayEvent instance = new FireDelayEvent();

	public FireDelayEvent() {
	}

	public String getPackageKey() {
		return null;
	}

	public String getEventKey() {
		return KEY;
	}

}
