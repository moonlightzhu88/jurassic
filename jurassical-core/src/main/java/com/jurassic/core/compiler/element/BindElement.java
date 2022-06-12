package com.jurassic.core.compiler.element;

import com.jurassic.core.progress.handler.pin.express.Express;

/**
 * �������ӱ�ǩ
 * <bind from="..." to="..."/>
 * 
 * @author yzhu
 * 
 */
public class BindElement extends Element {

	private String _from;// ���ӵ����task
	private String _to;// ���ӵ��յ�task
	private ExpressElement _condition;// �������ʽ����ѡ��

	public void setFrom(String from) {
		this._from = from;
	}

	public String getFrom() {
		return this._from;
	}

	public void setTo(String to) {
		this._to = to;
	}

	public String getTo() {
		return this._to;
	}

	public void setCondition(ExpressElement condition) {
		this._condition = condition;
	}

	public ExpressElement getCondition() {
		return this._condition;
	}

	public String toXml() {
		StringBuilder buf = new StringBuilder();
		buf.append("<bind from=\"").append(this._from)
				.append("\" to=\"").append(this._to).append("\">\r\n");
		if (this._condition != null) {
			buf.append(this._condition.toXml());
		}
		buf.append("</bind>\r\n");
		return buf.toString();
	}
}
