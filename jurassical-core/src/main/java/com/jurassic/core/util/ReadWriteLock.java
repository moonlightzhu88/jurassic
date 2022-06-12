package com.jurassic.core.util;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

import sun.misc.Unsafe;

/**
 * ��д��
 * 
 * ���������ɶ���̻߳��,д��ֻ����1���̻߳�ȡ,���Ҷ�д������
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
	private volatile int value;// ���ĵ�ǰֵ

	/**
	 * ��ȡ����,��value>=0��ʱ��,���Ի�ö���,������Ҫ�ȴ�
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
