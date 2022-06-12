package com.jurassic.core.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * 简单互斥锁,采用自旋快速检测的方式
 * 
 * @author yzhu
 *
 */
public class SimpleMutexLock {
	private final AtomicInteger _lock = new AtomicInteger(0);
	
	/**
	 * 加锁
	 */
	public void lock(){
		while (!this._lock.compareAndSet(0, 1)){
			LockSupport.parkNanos(1L);
		}
	}
	
	/**
	 * 解锁
	 */
	public void unlock(){
		this._lock.set(0);
	}
}
