package com.jurassic.core.util;

/**
 * 可分配元素的数组
 * 
 * @author yzhu
 * 
 */
public abstract class MallocedArray {

	private final SimpleMutexLock _lock;// 支持线程安全性的互斥锁
	protected Object[] _array;// 存储value的数组
	protected int _capacity;// 数组的容量
	private int[] _nexts;// 空闲队列的下一个节点指针
	private int _free;// 空闲队列的头指针
	private final int _batchSize;// 每次扩展的容量

	public MallocedArray(boolean safe, int batchSize) {
		if (safe) {
			this._lock = new SimpleMutexLock();
		} else {
			this._lock = null;
		}
		this._batchSize = batchSize;
		this._array = new Object[batchSize];
		this._capacity = batchSize;
		this._nexts = new int[batchSize];
		for (int i = 0; i < batchSize - 1; i++) {
			this._nexts[i] = i + 1;
		}
		// 最后一个节点的下一个空闲节点为空
		this._nexts[batchSize - 1] = -1;
		this._free = 0;
	}
	
	/**
	 * 创建一个对象
	 */
	protected abstract Object createObject(int index);

	/**
	 * 扩容batchSize
	 */
	private void expand() {
		int oldSize = this._capacity;
		int newSize = oldSize + this._batchSize;

		Object[] tmpValues = new Object[newSize];
		System.arraycopy(this._array, 0, tmpValues, 0, oldSize);
		for (int i = oldSize; i < newSize; i++) {
			tmpValues[i] = this.createObject(i);
		}

		int[] tmpNexts = new int[newSize];
		System.arraycopy(this._nexts, 0, tmpNexts, 0, oldSize);
		for (int i = oldSize; i < newSize - 1; i++) {
			tmpNexts[i] = i + 1;
		}
		tmpNexts[newSize - 1] = -1;

		this._array = tmpValues;
		this._capacity = newSize;
		this._nexts = tmpNexts;
		this._free = oldSize;
	}

	/**
	 * 从空闲队列中分配一个存储单元用于存储value
	 * 如果空闲队列已经空了,则自动扩展batchSize
	 */
	protected int malloc() {
		if (this._free == -1) {
			// 没有可分配的空闲单元
			this.expand();
		}

		int malloc = this._free;
		this._free = this._nexts[malloc];
		this._nexts[malloc] = -1;

		return malloc;
	}

	/**
	 * 回收资源
	 */
	protected void free(int index) {
		this._nexts[index] = this._free;
		this._free = index;
	}
	
	protected void lock(){
		if (this._lock != null) {
			this._lock.lock();
		}
	}
	
	protected void unlock(){
		if (this._lock != null) {
			this._lock.unlock();
		}
	}
}
