package com.jurassic.core.compiler.element;

/**
 * catch-table的元素
 * 
 * <entry from="..." to="..." except="..."/>
 * 
 * @author yzhu
 * 
 */
public class EntryElement extends Element {

	private String _checkFrom;// 检测异常的开始<task/>的name
	private String _checkTo;// 检测异常的结束<task/>的name
	private String _except;// 异常代码流程的执行入口<task/>的name

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
