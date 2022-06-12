package com.jurassic.core.event;

/**
 * 触发延迟事件执行
 * 定期向各个处理器发送
 * 
 * @author yzhu
 *
 */
public class FireDelayEvent extends Event {
	
	public static final String KEY = "timer";
	
	// 触发的singleton实例
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
