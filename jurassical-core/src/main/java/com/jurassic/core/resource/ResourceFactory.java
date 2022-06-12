package com.jurassic.core.resource;

import java.util.Map;

/**
 * 资源工厂接口
 *
 * @author yzhu
 */
public interface ResourceFactory<T> {

	/**
	 * 初始化资源工厂
	 */
	void init(Map<String, String> params) throws Throwable;

	/**
	 * 创建一个新资源
	 */
	Resource<T> newResource();
}
