package com.jurassic.core.progress;

import com.jurassic.core.progress.handler.pin.PinMetaInfo;

/**
 * ����Ԫ���ݶ���ӿ�
 * 
 * @author yzhu
 * 
 */
public interface ProgressMetaInfo {

	/**
	 * ������̵�����
	 */
	String getProgressKey();

	/**
	 * �����������ܽŶ���
	 */
	PinMetaInfo[] getInputPins();

	/**
	 * �����������ܽŶ���
	 */
	PinMetaInfo getOutputPins();
}
