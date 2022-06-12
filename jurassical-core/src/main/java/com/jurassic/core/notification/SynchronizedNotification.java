package com.jurassic.core.notification;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

/**
 * 同步的结果回调接口
 *
 * @author yzhu
 * 
 */
public class SynchronizedNotification implements ResultNotification {

	private Thread _owner;// 流程使用者所在的线程，主线程会在其上执行wait操作
	private Object _result;// 流程的输出结果

	/**
	 * 设置该notification需要通知的线程
	 */
	public void setOwner(Thread owner) {
		this._owner = owner;
	}

	public void notify(Object result) {
		this._result = result;

		// 唤醒等待的线程
		LockSupport.unpark(this._owner);
	}

	/**
	 * 转换输出数据,如果是Throwable抛出Throwable
	 */
	private Object _getResult() throws Throwable {
		// 如果结果是异常信息，则抛出
		// 否则直接返回结果
		if (this._result instanceof Throwable) {
			throw (Throwable) this._result;
		} else {
			return this._result;
		}
	}

	/**
	 * 获得任务的输出
	 * 带有超时时间
	 */
	public Object getResult(long timeoutMillSeconds) throws Throwable {
		// 如果已经有结果了,则直接返回
		if (this._result != null) {
			return this._getResult();
		}

		// 等待结果返回，线程被重新唤醒
		long start = System.currentTimeMillis();
		LockSupport.parkNanos(timeoutMillSeconds * 1000000L);
		long end = System.currentTimeMillis();
		// 等待时间结束后,如果没有结果则执行超时,否则直接返回结果
		if (this._result == null) {
			if (end - start >= timeoutMillSeconds) {
				// 如果没有被提前唤醒，则抛出超时异常
				throw new TimeoutException("get result timeout");
			} else {
				// 这种情况说明返回就是null
				return null;
			}
		} else {
			// 如果已经有返回数据了，直接返回
			return this._getResult();
		}
	}
}
