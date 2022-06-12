package com.jurassic.core.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.jurassic.core.classloader.ComponentClassLoader;
import com.jurassic.core.resource.AttachedResourceTbl;

/**
 * �����ʹ�õ��ض��̳߳�
 * 
 * @author yzhu
 * 
 */
public class ComponentThreadPool extends ThreadPoolExecutor {

	private final ComponentClassLoader _clzLoader;// ����İ���Ӧ���������,���������Ҳ��Ϊ�̵߳������ļ�����
	private final AttachedResourceTbl _attachedResourceTbl;// �߳�ʹ�õ���Դӳ���

	public ComponentThreadPool(int nThreads, ThreadFactory factory, ComponentClassLoader clzLoader,
			AttachedResourceTbl mapping) {
		super(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<>(), factory);
		this._clzLoader = clzLoader;
		this._attachedResourceTbl = mapping;
	}

	/**
	 * �߳�ִ��ǰ�������̵߳��������������
	 * ͬʱ��Դӳ���ϰ󶨵�ǰ�߳�
	 */
	protected void beforeExecute(Thread thread, Runnable runnable) {
		thread.setContextClassLoader(this._clzLoader);
		this._attachedResourceTbl.attachThread(thread);
	}

	/**
	 * �߳�ִ�н�������������֮ǰ�������Դ
	 */
	protected void afterExecute(Runnable runnable, Throwable error) {
		Thread thread = Thread.currentThread();
		this._attachedResourceTbl.detachThread(thread);
		// �����̵߳��������������
		thread.setContextClassLoader(ClassLoader.getSystemClassLoader());
	}
}
