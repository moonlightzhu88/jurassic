package com.jurassic.core.bus.error;

/**
 * ���û���ҵ�����
 * 
 * @author yzhu
 *
 */
public class ComponentNotFoundErr extends Exception {

	public ComponentNotFoundErr(String handlerKey){
		super("component " + handlerKey + " not found");
	}
}
