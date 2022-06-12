package com.jurassic.core.compiler.element;

/**
 * ���̲�����ǩ,����input��output�Ķ���
 * <input type="..." desc="..."/>
 * <output type="..." desc="..."/>
 * 
 * @author yzhu
 *
 */
public class ProgressParamElement extends Element {
	private boolean _input;// �Ƿ����������
	private String _type;// �������ݵ�����
	private String _desc;// �����ı�

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
