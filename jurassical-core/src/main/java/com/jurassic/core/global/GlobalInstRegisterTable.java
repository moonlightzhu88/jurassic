package com.jurassic.core.global;

import java.util.HashMap;
import java.util.Map;

/**
 * ϵͳ����ȫ��ʵ��ע���
 * 
 * @author yzhu
 * 
 */
public class GlobalInstRegisterTable {

	private static final Map<String, Object> _globalInstanceTbl = new HashMap<>();
	
	/**
	 * ע��ȫ��ʵ��
	 */
	public static void register(String key, Object instance){
		_globalInstanceTbl.put(key, instance);
	}
	
	/**
	 * ����ȫ��inst
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getInst(String key, Class<T> clz){
		return (T) _globalInstanceTbl.get(key);
	}

}
