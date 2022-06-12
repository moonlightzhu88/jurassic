package com.jurassic.core.bus.error;

/**
 * 事件没有触发错误
 * 
 * @author yzhu
 *
 */
public class EventNotFiredErr extends Exception {

	public EventNotFiredErr(String msg){
		super(msg);
	}
}
