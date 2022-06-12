package com.jurassic.core.resource;

import java.util.Map;

/**
 * ��Դ������,�������е���Դ����
 *
 * @author yzhu
 */
public interface ResourceFactoryTbl {
	String GLOBAL_KEY = "res_factory_pool";// jurassic��Դ�����id

	/**
	 * ����һ����Դ����
	 */
	void loadResourceFactory(String clz, String id,
									Map<String, String> params) throws Throwable;

	/**
	 * ���ݹ������ƻ�ö�Ӧ����Դ����
	 */
	ResourceFactory<?> getResourceFactory(String id);
}
