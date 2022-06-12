package com.jurassic.core.resource;

import java.util.Map;

/**
 * 资源工厂表,管理所有的资源工厂
 *
 * @author yzhu
 */
public interface ResourceFactoryTbl {
	String GLOBAL_KEY = "res_factory_pool";// jurassic资源共享池id

	/**
	 * 创建一个资源工厂
	 */
	void loadResourceFactory(String clz, String id,
									Map<String, String> params) throws Throwable;

	/**
	 * 根据工厂名称获得对应的资源工厂
	 */
	ResourceFactory<?> getResourceFactory(String id);
}
