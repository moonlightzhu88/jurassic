package com.jurassic.core.compiler.element;

/**
 * ���̶���Ļ�������Ԫ�ض���
 * 
 * @author yzhu
 * 
 */
public abstract class Element {

	// ��ǩ�����ƣ�����ͨ����Ϊ��������Ϣ�е�һ��������Ϣ
	// name�����Ǳ��������
	protected String _name;

	public void setName(String name) {
		this._name = name;
	}

	public String getName() {
		return this._name;
	}

	public abstract String toXml();

}
