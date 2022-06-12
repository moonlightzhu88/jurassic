package com.jurassic.core.util;

/**
 * 对象泛型的MallocedArray扩展类
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
			// 分配一个存储空间
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
