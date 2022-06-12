package com.jurassic.core.compiler.element;

import java.util.List;

/**
 * ������ǩ
 * <param index="..."/>
 * 
 * @author yzhu
 * 
 */
public class ParamElement extends PinElement {

	private int _index;// ������λ������

	public void setIndex(String index) {
		this._index = Integer.parseInt(index);
	}

	public int getIndex() {
		return this._index;
	}

	public int getPinType() {
		return PinElement.T_PARAM;
	}

	public List<String> getRefPins() {
		return null;
	}

	public String toXml() {
		StringBuilder buf = new StringBuilder();

		buf.append("<param");
		if (this._name != null) {
			buf.append(" name=\"").append(this._name).append("\"");
		}
		buf.append(" index=\"").append(this._index).append("\"/>\r\n");

		return buf.toString();
	}

}
