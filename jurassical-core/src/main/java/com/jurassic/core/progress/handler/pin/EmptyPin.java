package com.jurassic.core.progress.handler.pin;

/**
 * �չܽ�
 * ͨ����ʾһЩ��ֵ
 * ����ȫ�ֵ�һʵ��
 *
 * @author yzhu
 */
public class EmptyPin extends Pin {
	
	public static EmptyPin inst = new EmptyPin();

	public Object getData() {
		return null;
	}

}
