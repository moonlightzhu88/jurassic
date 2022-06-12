package com.jurassic.core.progress.factory;

import com.jurassic.core.progress.Progress;
import com.jurassic.core.progress.handler.pin.Pin;

/**
 * ���̹����ӿ�
 * 
 * @author yzhu
 * 
 */
public interface ProgressTemplate {

	/**
	 * ����һ������������
	 * ÿһ�����̶�Ӧһ��Generator
	 */
	void initProgress(Progress progress, Pin[] params) throws Exception;

	/**
	 * �������̵�key
	 */
	String getProgressKey();

}
