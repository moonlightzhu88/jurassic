package com.jurassic.core.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Ӧ���ڵ���д����,����������Ĳ�������
 * 
 * �ӿڶ�������map���ݽṹ,���������2�����ݸ���
 * 
 * ��������һ��ֻ�������ݸ����Ͻ���,���Բ�ʹ����,֧�ָ���Ĳ�������
 * 
 * д��������һ���ɶ���д�����ݸ����Ͻ���,��һ����д����������,ʹ��flush����,������ͬ����ֻ�������ݸ�����
 * 
 * @author yzhu
 * 
 */
public class CopyOnWriteMap<K, V> {
	
	private volatile Map<K, V> _readOnly = new HashMap<>();// ֻ��������
	private Map<K, V> _writeOnly = new HashMap<>();// ����д������
	
	/**
	 * �ȼ���map.get����
	 */
	public V get(K key){
		return this._readOnly.get(key);
	}
	
	/**
	 * ��ö����ݵ�size
	 */
	public int size(){
		return this._readOnly.size();
	}
	
	/**
	 * �ȼ���map.set����
	 */
	public synchronized void set(K key, V val){
		this._writeOnly.put(key, val);
	}

	public synchronized void setAll(Map<K, V> mapping) {
		this._writeOnly.putAll(mapping);
	}

	/**
	 * �������
	 */
	public synchronized void clear() {
		this._writeOnly.clear();
	}
	
	public synchronized V remove(K key){
		return this._writeOnly.remove(key);
	}
	
	/**
	 * �����µ���������һ����copy��readOnly��
	 */
	public synchronized void flush(){
		// ��_readOnly��_writeOnly���л���
		this._readOnly = this._writeOnly;
		this._writeOnly = new HashMap<>();
		// ��������_readOnly�е������ݸ�ֵ��_writeOnly��(�Ի�ǰ��_readOnly)
		this._writeOnly.putAll(this._readOnly);
	}

	/**
	 * ���ֻ��map
	 */
	public Map<K, V> toMap() {
		return this._readOnly;
	}
}
