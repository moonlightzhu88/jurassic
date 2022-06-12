package com.jurassic.core.util;

/**
 * �ɷ���Ԫ�ص�����
 * 
 * @author yzhu
 * 
 */
public abstract class MallocedArray {

	private final SimpleMutexLock _lock;// ֧���̰߳�ȫ�ԵĻ�����
	protected Object[] _array;// �洢value������
	protected int _capacity;// ���������
	private int[] _nexts;// ���ж��е���һ���ڵ�ָ��
	private int _free;// ���ж��е�ͷָ��
	private final int _batchSize;// ÿ����չ������

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
		// ���һ���ڵ����һ�����нڵ�Ϊ��
		this._nexts[batchSize - 1] = -1;
		this._free = 0;
	}
	
	/**
	 * ����һ������
	 */
	protected abstract Object createObject(int index);

	/**
	 * ����batchSize
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
	 * �ӿ��ж����з���һ���洢��Ԫ���ڴ洢value
	 * ������ж����Ѿ�����,���Զ���չbatchSize
	 */
	protected int malloc() {
		if (this._free == -1) {
			// û�пɷ���Ŀ��е�Ԫ
			this.expand();
		}

		int malloc = this._free;
		this._free = this._nexts[malloc];
		this._nexts[malloc] = -1;

		return malloc;
	}

	/**
	 * ������Դ
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
