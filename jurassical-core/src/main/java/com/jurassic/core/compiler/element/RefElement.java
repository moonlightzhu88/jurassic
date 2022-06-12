package com.jurassic.core.compiler.element;

import java.util.ArrayList;
import java.util.List;

/**
 * 引用标签
 * <ref ref-name="..."/>
 * 
 * @author yzhu
 * 
 */
public class RefElement extends PinElement {

	private String _refName;// 引用的数据管脚名称

	public void setRefName(String refName) {
		this._refName = refName;
	}

	public String getRefName() {
		return this._refName;
	}

	public int getPinType() {
		return PinElement.T_REF;
	}

	public List<String> getRefPins() {
		List<String> refPins = new ArrayList<>();
		refPins.add(this._refName);
		return refPins;
	}

	public String toXml() {
		StringBuilder buf = new StringBuilder();

		buf.append("<ref");
		if (this._name != null) {
			buf.append(" name=\"").append(this._name).append("\"");
		}
		buf.append(" ref-name=\"").append(this._refName).append("\"/>\r\n");
		
		return buf.toString();
	}
}
