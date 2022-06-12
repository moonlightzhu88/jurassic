package com.jurassic.core.compiler.element;

/**
 * 流程参数标签,包含input和output的定义
 * <input type="..." desc="..."/>
 * <output type="..." desc="..."/>
 * 
 * @author yzhu
 *
 */
public class ProgressParamElement extends Element {
	private boolean _input;// 是否是输入参数
	private String _type;// 输入数据的类型
	private String _desc;// 描述文本

	public void setInputFlag(boolean input) {
		this._input = input;
	}

	public boolean isInput() {
		return this._input;
	}

	public void setType(String type) {
		this._type = type;
	}

	public String getType() {
		return this._type;
	}

	public void setDesc(String desc) {
		this._desc = desc;
	}

	public String getDesc() {
		return this._desc;
	}

	public String toXml() {
		StringBuilder buf = new StringBuilder();

		if (this._input)
			buf.append("<input");
		else
			buf.append("<output");
		buf.append(" type=\"").append(this._type).append("\" desc=\"")
				.append(this._desc).append("\"/>\r\n");

		return buf.toString();
	}
}
