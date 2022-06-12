package com.jurassic.core.compiler.element;

import java.util.List;

/**
 * 类定义标签
 * <class class="..."/>
 *
 * @author yzhu
 */
public class ClassElement extends PinElement {

	private String _className;// 类名称

	public void setClassName(String className) {
		this._className = className;
	}

	public String getClassName() {
		return this._className;
	}

	public int getPinType() {
		return PinElement.T_CLASS;
	}

	public List<String> getRefPins() {
		return null;
	}

	public String toXml() {
		StringBuilder buf = new StringBuilder();

		buf.append("<class");
		if (this._name != null) {
			buf.append(" name=\"").append(this._name).append("\"");
		}
		buf.append(" class=\"").append(this._className).append("\"/>\r\n");

		return buf.toString();
	}

}
