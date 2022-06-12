package com.jurassic.core.event;

/**
 * 事件执行的封装类
 * 处理器的事件缓冲区中单个元素类型
 * 
 * @author yzhu
 */
public class EventWrapper<T extends Event> {

	private T _event;// 封装的事件

	/**
	 * 获得需要处理的实际事件
	 */
	public T getEvent() {
		return _event;
	}

	/**
	 * 设置需要处理的实际事件
	 */
	public void setEvent(T event) {
		this._event = event;
	}

	/**
	 * 重置缓冲区事件
	 */
	public void reset() {
		this._event = null;
	}
}
