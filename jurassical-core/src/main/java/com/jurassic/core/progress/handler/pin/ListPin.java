package com.jurassic.core.progress.handler.pin;

import java.util.ArrayList;
import java.util.List;

/**
 * �洢�б����ݵĹܽ�
 *
 * @author yzhu
 *
 */
public class ListPin extends Pin {

	private final List<Pin> _pins = new ArrayList<>();// �����б�
	private List<Object> _cache;// �������

	public void add(Pin pin) {
		this._pins.add(pin);
	}

	public void addData(Object data) {
		if (this._cache == null)
			this._cache = new ArrayList<>();
		this._cache.add(data);
	}

	public Object getData() {
		if (this._cache != null)
			return this._cache;
		this._cache = new ArrayList<>();
		for (Pin pin : this._pins) {
			this._cache.add(pin.getData());
		}
		return this._cache;
	}

	public int getSize() {
		return this._pins.size();
	}

}
