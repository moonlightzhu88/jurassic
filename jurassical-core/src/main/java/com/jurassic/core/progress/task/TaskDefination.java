package com.jurassic.core.progress.task;

import com.jurassic.core.progress.handler.pin.PinMetaInfo;

/**
 * 任务定义接口
 * 
 * @author yzhu
 * 
 */
public interface TaskDefination {

	/**
	 * task的定义
	 */
	String getTaskKey();

	/**
	 * 获得输入管脚定义
	 */
	PinMetaInfo[] getInputPins();

	/**
	 * 获得输出管脚定义
	 */
	PinMetaInfo[] getOutputPins();

}
