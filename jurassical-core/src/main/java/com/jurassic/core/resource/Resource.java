package com.jurassic.core.resource;

/**
 * Jurassic��������Դ�ӿ�
 *
 * @author yzhu
 */
public interface Resource<T> {

	/**
	 * �����Դ����
	 */
	T getResource();

	/**
	 * ������Դ
	 */
	void destroy();
}
