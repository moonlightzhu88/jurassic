package com.jurassic.core.progress.handler.pin;

/**
 * �洢��������Ĺܽ�
 * 
 * @author yzhu
 * 
 */
public class ObjectPin extends Pin {

	private Object _data;// ���ݶ���

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
