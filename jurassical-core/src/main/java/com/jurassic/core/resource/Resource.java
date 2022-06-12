package com.jurassic.core.resource;

/**
 * Jurassic服务器资源接口
 *
 * @author yzhu
 */
public interface Resource<T> {

	/**
	 * 获得资源对象
	 */
	T getResource();

	/**
	 * 销毁资源
	 */
	void destroy();
}
