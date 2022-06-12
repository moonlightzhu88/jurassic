package com.jurassic.core.util;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

import sun.misc.Unsafe;

/**
 * 读写锁
 * 
 * 读锁可以由多个线程获得,写锁只能由1个线程获取,并且读写锁互斥
 * 
 * @author yzhu
 * 
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class ReadWriteLock {

	private static final Unsafe unsafe;
	private static final long valueOffset;

	static {
		PrivilegedExceptionAction action;
		try {
			action = () -> {
				Field theUnsafe = Unsafe.class
						.getDeclaredField("theUnsafe");
				theUnsafe.setAccessible(true);
				return ((Unsafe) theUnsafe.get(null));
			};
			unsafe = (Unsafe) AccessController.doPrivileged(action);
			valueOffset = unsafe.objectFieldOffset(ReadWriteLock.class
					.getDeclaredField("value"));
		} catch (Exception e) {
			throw new RuntimeException("Unable to load unsafe", e);
		}
	}

	@SuppressWarnings("unused")
	private volatile int value;// 锁的当前值

	/**
	 * 获取读锁,在value>=0的时候,可以获得读锁,否则需要等待
	 */
	public void readLock() {
		int curr;
		while (true) {
			curr = this.value;
			if (curr < 0) {
				unsafe.park(false, 1L);
				continue;
			}

			if (unsafe.compareAndSwapInt(this, valueOffset, curr, curr + 1)) {
				break;
			} else {
				unsafe.park(false, 1L);
			}
		}
	}

	public void readUnLock() {
		unsafe.getAndAddInt(this, valueOffset, -1);
	}

	public void writeLock() {
		while (!unsafe.compareAndSwapInt(this, valueOffset, 0, -1)) {
			unsafe.park(false, 1L);
		}
	}

	public void writeUnLock() {
		unsafe.getAndAddInt(this, valueOffset, 1);
	}
}
