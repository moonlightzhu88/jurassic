package com.jurassic.core.progress.handler.pin;

/**
 * 数据管脚定义
 * 数据管脚封装了处理器之间相互传输的数据
 *
 * @author yzhu
 *
 */
public abstract class Pin {

	/**
	 * 获得数据管脚上的真实数据
	 */
	public abstract Object getData();
	
}
