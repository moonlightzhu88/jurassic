package com.jurassic.core.compiler.element;

/**
 * catch-table��Ԫ��
 * 
 * <entry from="..." to="..." except="..."/>
 * 
 * @author yzhu
 * 
 */
public class EntryElement extends Element {

	private String _checkFrom;// ����쳣�Ŀ�ʼ<task/>��name
	private String _checkTo;// ����쳣�Ľ���<task/>��name
	private String _except;// �쳣�������̵�ִ�����<task/>��name

	public void setFrom(String from) {
		this._checkFrom = from;
	}

	public String getFrom() {
		return this._checkFrom;
	}

	public void setTo(String to) {
		this._checkTo = to;
	}

	public String getTo() {
		return this._checkTo;
	}

	public void setExcept(String except) {
		this._except = except;
	}

	public String getExcept() {
		return this._except;
	}

	public String toXml() {

		String buf = "<entry from=\"" + this._checkFrom + "\" to=\"" +
				this._checkTo + "\" except=\"" + this._except +
				"\"/>\r\n";

		return buf;
	}
}
