package com.jurassic.core.progress;

import com.jurassic.core.progress.handler.pin.PinMetaInfo;

/**
 * 流程元数据定义接口
 * 
 * @author yzhu
 * 
 */
public interface ProgressMetaInfo {

	/**
	 * 获得流程的名称
	 */
	String getProgressKey();

	/**
	 * 获得流程输入管脚定义
	 */
	PinMetaInfo[] getInputPins();

	/**
	 * 获得流程输出管脚定义
	 */
	PinMetaInfo getOutputPins();
}
