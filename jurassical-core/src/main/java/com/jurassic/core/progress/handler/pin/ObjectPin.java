package com.jurassic.core.progress.handler.pin;

/**
 * 存储单个对象的管脚
 * 
 * @author yzhu
 * 
 */
public class ObjectPin extends Pin {

	private Object _data;// 数据对象

	public ObjectPin() {}

	public ObjectPin(Object data) {
		this._data = data;
	}

	public void setData(Object data) {
		this._data = data;
	}

	public Object getData() {
		return this._data;
	}

}
