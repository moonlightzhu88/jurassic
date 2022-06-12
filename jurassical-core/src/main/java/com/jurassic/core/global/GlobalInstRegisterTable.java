package com.jurassic.core.global;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统所有全局实例注册表
 * 
 * @author yzhu
 * 
 */
public class GlobalInstRegisterTable {

	private static final Map<String, Object> _globalInstanceTbl = new HashMap<>();
	
	/**
	 * 注册全局实例
	 */
	public static void register(String key, Object instance){
		_globalInstanceTbl.put(key, instance);
	}
	
	/**
	 * 搜索全局inst
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getInst(String key, Class<T> clz){
		return (T) _globalInstanceTbl.get(key);
	}

}
