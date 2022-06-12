package com.jurassic.core.compiler.element;

import com.jurassic.core.progress.handler.pin.express.Express;

/**
 * 流程连接标签
 * <bind from="..." to="..."/>
 * 
 * @author yzhu
 * 
 */
public class BindElement extends Element {

	private String _from;// 连接的起点task
	private String _to;// 连接的终点task
	private ExpressElement _condition;// 条件表达式（可选）

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
