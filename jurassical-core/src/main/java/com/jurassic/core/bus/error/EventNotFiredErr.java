package com.jurassic.core.bus.error;

/**
 * �¼�û�д�������
 * 
 * @author yzhu
 *
 */
public class EventNotFiredErr extends Exception {

	public EventNotFiredErr(String msg){
		super(msg);
	}
}
