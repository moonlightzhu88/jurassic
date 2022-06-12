package com.jurassic.core.bus.error;

/**
 * 组件没有找到错误
 * 
 * @author yzhu
 *
 */
public class ComponentNotFoundErr extends Exception {

	public ComponentNotFoundErr(String handlerKey){
		super("component " + handlerKey + " not found");
	}
}
