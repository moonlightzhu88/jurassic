package com.jurassic.core.compiler.element;

import java.util.List;

/**
 * ��̬ʵ����ǩ
 * <static-object class="..." instance="..."/>
 * 
 * @author yzhu
 * 
 */
public class StaticObjectElement extends PinElement {

	private String _className;// ��
	private String _instance;// ��̬ʵ������

	public void setClassName(String className) {
		this._className = className;
	}

	public String getClassName() {
		return this._className;
	}

	public void setInstance(String instance) {
		this._instance = instance;
	}

	public String getInstance() {
		return this._instance;
	}

	public int getPinType() {
		return PinElement.T_STATIC;
	}

	public List<String> getRefPins() {
		return null;
	}

	public String toXml() {
		StringBuilder buf = new StringBuilder();

		buf.append("<static-object");
		if (this._name != null) {
			buf.append(" name=\"").append(this._name).append("\"");
		}
		buf.append(" class=\"").append(this._className).append("\"");
		buf.append(" instance=\"").append(this._instance).append("\"/>\r\n");

		return buf.toString();
	}
}
