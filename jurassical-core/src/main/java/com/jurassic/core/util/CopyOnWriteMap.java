package com.jurassic.core.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 应用于单个写操作,多个读操作的并发环境
 * 
 * 接口定义类似map数据结构,整个类采用2个数据副本
 * 
 * 读操作在一个只读的数据副本上进行,可以不使用锁,支持更大的并发访问
 * 
 * 写操作在另一个可读可写的数据副本上进行,在一次性写完多个操作后,使用flush操作,将数据同步到只读的数据副本上
 * 
 * @author yzhu
 * 
 */
public class CopyOnWriteMap<K, V> {
	
	private volatile Map<K, V> _readOnly = new HashMap<>();// 只读的数据
	private Map<K, V> _writeOnly = new HashMap<>();// 用于写的数据
	
	/**
	 * 等价于map.get方法
	 */
	public V get(K key){
		return this._readOnly.get(key);
	}
	
	/**
	 * 获得读数据的size
	 */
	public int size(){
		return this._readOnly.size();
	}
	
	/**
	 * 等价于map.set方法
	 */
	public synchronized void set(K key, V val){
		this._writeOnly.put(key, val);
	}

	public synchronized void setAll(Map<K, V> mapping) {
		this._writeOnly.putAll(mapping);
	}

	/**
	 * 清空数据
	 */
	public synchronized void clear() {
		this._writeOnly.clear();
	}
	
	public synchronized V remove(K key){
		return this._writeOnly.remove(key);
	}
	
	/**
	 * 将更新的所有数据一次性copy到readOnly中
	 */
	public synchronized void flush(){
		// 将_readOnly和_writeOnly进行互换
		this._readOnly = this._writeOnly;
		this._writeOnly = new HashMap<>();
		// 将交换到_readOnly中的新数据赋值到_writeOnly中(对换前的_readOnly)
		this._writeOnly.putAll(this._readOnly);
	}

	/**
	 * 获得只读map
	 */
	public Map<K, V> toMap() {
		return this._readOnly;
	}
}
