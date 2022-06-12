package com.jurassic.core.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.jurassic.core.util.CopyOnWriteMap;

/**
 * 线程绑定资源的映射表
 * 每个线程使用的资源都会被记录在该表中
 * 在线程生命周期结束的时候，这些被绑定在该线程上的资源会进行资源的释放
 * 
 * @author yzhu
 * 
 */
public class AttachedResourceTbl {

	private final CopyOnWriteMap<Thread, Map<String, Resource<?>>> _attachedResources
			= new CopyOnWriteMap<>();// 每个线程使用的资源映射表

	/**
	 * 将资源映射表和对应线程绑定
	 */
	public void attachThread(Thread thread) {
		// 为每一个线程分配资源表(空表)
		Map<String, Resource<?>> resourceTbl = new HashMap<>();
		this._attachedResources.set(thread, resourceTbl);
		this._attachedResources.flush();
	}

	/**
	 * 去除线程和资源映射表的关联
	 */
	public void detachThread(Thread thread) {
		// 销毁线程使用的资源
		Map<String, Resource<?>> tbl = this._attachedResources.get(thread);
		for (Entry<String, Resource<?>> entry : tbl.entrySet()) {
			Resource<?> resource = entry.getValue();
			resource.destroy();
		}
		tbl.clear();
		this._attachedResources.remove(thread);
		this._attachedResources.flush();
	}

	/**
	 * 获得当前线程中name指定的资源
	 */
	@SuppressWarnings("unchecked")
	public <T> T getResource(String name) {
		Thread current = Thread.currentThread();
		Map<String, Resource<?>> mapping = this._attachedResources.get(current);
		Resource<?> resource = mapping.get(name);
		if (resource != null) {
			return (T) resource.getResource();
		}
		return null;
	}

	/**
	 * 将特定的资源绑定到当前线程上
	 */
	public void attachResource(String name, Resource<?> resource) {
		Thread current = Thread.currentThread();
		Map<String, Resource<?>> mapping = this._attachedResources.get(current);
		mapping.put(name, resource);
	}
}
