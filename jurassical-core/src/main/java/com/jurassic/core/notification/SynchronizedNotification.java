package com.jurassic.core.notification;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

/**
 * ͬ���Ľ���ص��ӿ�
 *
 * @author yzhu
 * 
 */
public class SynchronizedNotification implements ResultNotification {

	private Thread _owner;// ����ʹ�������ڵ��̣߳����̻߳�������ִ��wait����
	private Object _result;// ���̵�������

	/**
	 * ���ø�notification��Ҫ֪ͨ���߳�
	 */
	public void setOwner(Thread owner) {
		this._owner = owner;
	}

	public void notify(Object result) {
		this._result = result;

		// ���ѵȴ����߳�
		LockSupport.unpark(this._owner);
	}

	/**
	 * ת���������,�����Throwable�׳�Throwable
	 */
	private Object _getResult() throws Throwable {
		// ���������쳣��Ϣ�����׳�
		// ����ֱ�ӷ��ؽ��
		if (this._result instanceof Throwable) {
			throw (Throwable) this._result;
		} else {
			return this._result;
		}
	}

	/**
	 * �����������
	 * ���г�ʱʱ��
	 */
	public Object getResult(long timeoutMillSeconds) throws Throwable {
		// ����Ѿ��н����,��ֱ�ӷ���
		if (this._result != null) {
			return this._getResult();
		}

		// �ȴ�������أ��̱߳����»���
		long start = System.currentTimeMillis();
		LockSupport.parkNanos(timeoutMillSeconds * 1000000L);
		long end = System.currentTimeMillis();
		// �ȴ�ʱ�������,���û�н����ִ�г�ʱ,����ֱ�ӷ��ؽ��
		if (this._result == null) {
			if (end - start >= timeoutMillSeconds) {
				// ���û�б���ǰ���ѣ����׳���ʱ�쳣
				throw new TimeoutException("get result timeout");
			} else {
				// �������˵�����ؾ���null
				return null;
			}
		} else {
			// ����Ѿ��з��������ˣ�ֱ�ӷ���
			return this._getResult();
		}
	}
}
