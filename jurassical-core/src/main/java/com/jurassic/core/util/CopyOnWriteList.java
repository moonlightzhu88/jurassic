package com.jurassic.core.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 和CopyOnWriteMap相似的功能,只是数据结构是List
 * 
 * @author yzhu
 * 
 * @param <V>
 */
public class CopyOnWriteList<V> {

	private volatile List<V> _readOnly;// 只读的数据
	private List<V> _writeOnly;// 用于写的数据

	public CopyOnWriteList() {
		this._readOnly = new ArrayList<>();
		this._writeOnly = new ArrayList<>();
	}

	public V get(int index) {
		return this._readOnly.get(index);
	}

	/**
	 * 添加一个元素
	 */
	public synchronized void add(V value) {
		this._writeOnly.add(value);
	}

	/**
	 * 删除元素
	 */
	public synchronized void remove(V value) {
		this._writeOnly.remove(value);
	}

	/**
	 * 清空writeOnly里面的数据
	 */
	public synchronized void clear() {
		this._writeOnly.clear();
	}

	/**
	 * 将写的数据刷新到读的数据中
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
