package com.jurassic.core.progress.task;

import com.jurassic.core.progress.handler.pin.PinMetaInfo;

/**
 * ������ӿ�
 * 
 * @author yzhu
 * 
 */
public interface TaskDefination {

	/**
	 * task�Ķ���
	 */
	String getTaskKey();

	/**
	 * �������ܽŶ���
	 */
	PinMetaInfo[] getInputPins();

	/**
	 * �������ܽŶ���
	 */
	PinMetaInfo[] getOutputPins();

}
