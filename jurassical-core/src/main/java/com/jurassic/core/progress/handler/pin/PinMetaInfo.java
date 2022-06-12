package com.jurassic.core.progress.handler.pin;

/**
 * 处理器的管脚定义
 * 
 * @author yzhu
 * 
 */
public class PinMetaInfo {

	// 类型定义常量
	public static final int TYPE_IN = 0;// 输入管脚
	public static final int TYPE_OUT = 1;// 输出管脚

	// 数据结构定义常量
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

	private final int _idx;// 管脚的位置,从0开始
	private final int _type;// 输入/输出管脚
	private final int _data;// 管脚的数据类型
	private final String _desc;// 管脚描述

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
