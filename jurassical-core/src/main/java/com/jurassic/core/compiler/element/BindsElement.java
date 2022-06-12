package com.jurassic.core.compiler.element;

import java.util.ArrayList;
import java.util.List;

/**
 * �������ӱ�ǩ
 * <binds>
 *     <bind .../>
 * </binds>
 * 
 * @author yzhu
 * 
 */
public class BindsElement extends Element {

	private final List<BindElement> _binds = new ArrayList<>();// ��������֮������ӹ�ϵ

	public void addBind(BindElement bind) {
		this._binds.add(bind);
	}

	public List<BindElement> getBinds() {
		return this._binds;
	}

	public String toXml() {
		StringBuilder buf = new StringBuilder();
		
		buf.append("<binds>\r\n");
		for (BindElement bind : this._binds) {
			buf.append(bind.toXml());
		}
		buf.append("</binds>\r\n");

		return buf.toString();
	}
}
