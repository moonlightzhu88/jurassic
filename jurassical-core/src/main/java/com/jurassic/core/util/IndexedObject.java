package com.jurassic.core.util;

/**
 * �����͵�MallocedArray��չ��
 * 
 * @author yzhu
 *
 * @param <V>
 */
public class IndexedObject<V> extends MallocedArray {

	public IndexedObject(boolean safe, int batchSize){
		super(safe, batchSize);
	}

	protected Object createObject(int index) {
		return null;
	}

	public int malloc(V value) {
		this.lock();
		try {
			// ����һ���洢�ռ�
			int malloc = this.malloc();
			this._array[malloc] = value;

			return malloc;
		} finally {
			this.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	public V remove(int index) {
		this.lock();
		try {
			V value = (V)this._array[index];
			this._array[index] = null;
			this.free(index);
			return value;
		} finally {
			this.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	public V get(int index) {
		return (V)this._array[index];
	}
}
