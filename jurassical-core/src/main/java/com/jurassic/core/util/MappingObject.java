package com.jurassic.core.util;

import java.util.*;

/**
 * ����ӳ�书�ܵĶ���ɷ�������
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
	
	private final Map<K, Integer> _mapping;// �洢λ�ú�key֮���ӳ���ϵ
	private ReadWriteLock _rwLock;// ��д��

	/**
	 * ɾ����key��value��
	 * ���û��key�򷵻�null
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
	 * set��key��value������value��ŵ���������
	 */
	public int set(K key, V value) {
		if (this._rwLock != null) {
			this._rwLock.writeLock();
		}
		try {
			Integer index = this._mapping.get(key);
			if (index != null) {
				// ���keyֵ֮ǰ�Ѿ����������ֱ�Ӹ���value
				this._array[index] = value;
				return index;
			}
			// ���û�����·���һ��λ��
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
	 * �����Ƿ���keyֵ�������ض�Ӧ�洢λ��
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
