package com.jurassic.core.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * �򵥻�����,�����������ټ��ķ�ʽ
 * 
 * @author yzhu
 *
 */
public class SimpleMutexLock {
	private final AtomicInteger _lock = new AtomicInteger(0);
	
	/**
	 * ����
	 */
	public void lock(){
		while (!this._lock.compareAndSet(0, 1)){
			LockSupport.parkNanos(1L);
		}
	}
	
	/**
	 * ����
	 */
	public void unlock(){
		this._lock.set(0);
	}
}
