package com.jurassic.core.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.jurassic.core.util.CopyOnWriteMap;

/**
 * �̰߳���Դ��ӳ���
 * ÿ���߳�ʹ�õ���Դ���ᱻ��¼�ڸñ���
 * ���߳��������ڽ�����ʱ����Щ�����ڸ��߳��ϵ���Դ�������Դ���ͷ�
 * 
 * @author yzhu
 * 
 */
public class AttachedResourceTbl {

	private final CopyOnWriteMap<Thread, Map<String, Resource<?>>> _attachedResources
			= new CopyOnWriteMap<>();// ÿ���߳�ʹ�õ���Դӳ���

	/**
	 * ����Դӳ���Ͷ�Ӧ�̰߳�
	 */
	public void attachThread(Thread thread) {
		// Ϊÿһ���̷߳�����Դ��(�ձ�)
		Map<String, Resource<?>> resourceTbl = new HashMap<>();
		this._attachedResources.set(thread, resourceTbl);
		this._attachedResources.flush();
	}

	/**
	 * ȥ���̺߳���Դӳ���Ĺ���
	 */
	public void detachThread(Thread thread) {
		// �����߳�ʹ�õ���Դ
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
	 * ��õ�ǰ�߳���nameָ������Դ
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
	 * ���ض�����Դ�󶨵���ǰ�߳���
	 */
	public void attachResource(String name, Resource<?> resource) {
		Thread current = Thread.currentThread();
		Map<String, Resource<?>> mapping = this._attachedResources.get(current);
		mapping.put(name, resource);
	}
}
