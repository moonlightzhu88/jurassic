package com.jurassic.core.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ��CopyOnWriteMap���ƵĹ���,ֻ�����ݽṹ��List
 * 
 * @author yzhu
 * 
 * @param <V>
 */
public class CopyOnWriteList<V> {

	private volatile List<V> _readOnly;// ֻ��������
	private List<V> _writeOnly;// ����д������

	public CopyOnWriteList() {
		this._readOnly = new ArrayList<>();
		this._writeOnly = new ArrayList<>();
	}

	public V get(int index) {
		return this._readOnly.get(index);
	}

	/**
	 * ���һ��Ԫ��
	 */
	public synchronized void add(V value) {
		this._writeOnly.add(value);
	}

	/**
	 * ɾ��Ԫ��
	 */
	public synchronized void remove(V value) {
		this._writeOnly.remove(value);
	}

	/**
	 * ���writeOnly���������
	 */
	public synchronized void clear() {
		this._writeOnly.clear();
	}

	/**
	 * ��д������ˢ�µ�����������
	 */
	public synchronized void flush() {
		this._readOnly = this._writeOnly;
		this._writeOnly = new ArrayList<>();
		this._writeOnly.addAll(this._readOnly);
	}

	public int size() {
		return this._readOnly.size();
	}
}
