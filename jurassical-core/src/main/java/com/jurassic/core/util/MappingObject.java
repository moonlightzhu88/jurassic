package com.jurassic.core.util;

import java.util.*;

/**
 * 带有映射功能的对象可分配数组
 * @author yzhu
 *
 * @param <K>
 * @param <V>
 */
public class MappingObject<K, V> extends IndexedObject<V> {

	public MappingObject(boolean safe, int batchSize){
		super(safe, batchSize);
		this._mapping = new HashMap<>();
		if (safe) {
			this._rwLock = new ReadWriteLock();
		}
	}
	
	private final Map<K, Integer> _mapping;// 存储位置和key之间的映射关系
	private ReadWriteLock _rwLock;// 读写锁

	/**
	 * 删除（key，value）
	 * 如果没有key则返回null
	 */
	public V remove(K key) {
		if (this._rwLock != null) {
			this._rwLock.writeLock();
		}
		try {
			Integer index = this._mapping.get(key);
			if (index == null)
				return null;

			V value = this.get(index);
			this.free(index);
			this._mapping.remove(key);
			return value;
		} finally {
			if (this._rwLock != null) {
				this._rwLock.writeUnLock();
			}
		}
	}

	/**
	 * set（key，value）返回value存放的数组索引
	 */
	public int set(K key, V value) {
		if (this._rwLock != null) {
			this._rwLock.writeLock();
		}
		try {
			Integer index = this._mapping.get(key);
			if (index != null) {
				// 如果key值之前已经分配过，则直接更新value
				this._array[index] = value;
				return index;
			}
			// 如果没有则新分配一个位置
			int malloc = this.malloc();
			this._array[malloc] = value;
			this._mapping.put(key, malloc);

			return malloc;
		} finally {
			if (this._rwLock != null) {
				this._rwLock.writeUnLock();
			}
		}
	}

	/**
	 * 查找是否含有key值，并返回对应存储位置
	 */
	public Integer containKey(K key) {
		if (this._rwLock != null) {
			this._rwLock.readLock();
		}
		try{
			return this._mapping.get(key);
		} finally {
			if (this._rwLock != null) {
				this._rwLock.readUnLock();
			}
		}
	}

	public Object[] getValues() {
		return this._array;
	}

	public Iterator<K> keys() {
		if (this._rwLock != null) {
			this._rwLock.readLock();
		}
		Iterator<K> it = this._mapping.keySet().iterator();
		if (this._rwLock != null) {
			this._rwLock.readUnLock();
		}
		return it;
	}
}
