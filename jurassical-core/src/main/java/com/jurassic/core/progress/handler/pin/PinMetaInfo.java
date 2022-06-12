package com.jurassic.core.progress.handler.pin;

/**
 * �������ĹܽŶ���
 * 
 * @author yzhu
 * 
 */
public class PinMetaInfo {

	// ���Ͷ��峣��
	public static final int TYPE_IN = 0;// ����ܽ�
	public static final int TYPE_OUT = 1;// ����ܽ�

	// ���ݽṹ���峣��
	public static final int VOID = 0;
	public static final int BYTE = 1;
	public static final int BOOLEAN = 2;
	public static final int SHORT = 3;
	public static final int CHAR = 4;
	public static final int INTEGER = 5;
	public static final int FLOAT = 6;
	public static final int LONG = 7;
	public static final int DOUBLE = 8;
	public static final int STRING = 9;
	public static final int DECIMAL = 10;
	public static final int OBJECT = 11;
	public static final int LIST = 12;
	public static final int INDEX = 13;
	public static final int DATE = 14;

	private final int _idx;// �ܽŵ�λ��,��0��ʼ
	private final int _type;// ����/����ܽ�
	private final int _data;// �ܽŵ���������
	private final String _desc;// �ܽ�����

	public PinMetaInfo(int idx, int type, int data, String desc) {
		this._idx = idx;
		this._type = type;
		this._data = data;
		this._desc = desc;
	}

	public int getIdx() {
		return this._idx;
	}

	public int getType() {
		return this._type;
	}

	public int getData() {
		return this._data;
	}

	public String getDesc() {
		return this._desc;
	}

}
