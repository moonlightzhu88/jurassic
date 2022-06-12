package com.jurassic.core.resource;

import java.util.Map;

/**
 * ��Դ�����ӿ�
 *
 * @author yzhu
 */
public interface ResourceFactory<T> {

	/**
	 * ��ʼ����Դ����
	 */
	void init(Map<String, String> params) throws Throwable;

	/**
	 * ����һ������Դ
	 */
	Resource<T> newResource();
}
