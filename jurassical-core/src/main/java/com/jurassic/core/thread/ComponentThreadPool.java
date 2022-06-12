package com.jurassic.core.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.jurassic.core.classloader.ComponentClassLoader;
import com.jurassic.core.resource.AttachedResourceTbl;

/**
 * 组件包使用的特定线程池
 * 
 * @author yzhu
 * 
 */
public class ComponentThreadPool extends ThreadPoolExecutor {

	private final ComponentClassLoader _clzLoader;// 部署的包对应的类加载器,这个加载器也作为线程的上下文加载器
	private final AttachedResourceTbl _attachedResourceTbl;// 线程使用的资源映射表

	public ComponentThreadPool(int nThreads, ThreadFactory factory, ComponentClassLoader clzLoader,
			AttachedResourceTbl mapping) {
		super(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<>(), factory);
		this._clzLoader = clzLoader;
		this._attachedResourceTbl = mapping;
	}

	/**
	 * 线程执行前，设置线程的上下文类加载器
	 * 同时资源映射上绑定当前线程
	 */
	protected void beforeExecute(Thread thread, Runnable runnable) {
		thread.setContextClassLoader(this._clzLoader);
		this._attachedResourceTbl.attachThread(thread);
	}

	/**
	 * 线程执行结束后销毁所有之前申请的资源
	 */
	protected void afterExecute(Runnable runnable, Throwable error) {
		Thread thread = Thread.currentThread();
		this._attachedResourceTbl.detachThread(thread);
		// 重置线程的上下文类加载器
		thread.setContextClassLoader(ClassLoader.getSystemClassLoader());
	}
}
