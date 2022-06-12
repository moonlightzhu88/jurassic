package com.jurassic.core.progress.handler.pin;

/**
 * 空管脚
 * 通常表示一些空值
 * 采用全局单一实例
 *
 * @author yzhu
 */
public class EmptyPin extends Pin {
	
	public static EmptyPin inst = new EmptyPin();

	public Object getData() {
		return null;
	}

}
