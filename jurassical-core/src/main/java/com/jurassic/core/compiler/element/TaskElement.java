package com.jurassic.core.compiler.element;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务标签
 * <task name="..." class="...">
 *     <property .../>
 *     <pin .../>
 * </task>
 * 
 * @author yzhu
 * 
 */
public class TaskElement extends Element {

	private String _className;// 类名
	private String _desc;// 描述文本
	private final List<PinElement> _inputs = new ArrayList<>();// 输入数据管脚

	public void setClassName(String className) {
		this._className = className;
	}

	public String getClassName() {
		return this._className;
	}

	public void setDesc(String desc) {
		this._desc = desc;
	}

	public String getDesc() {
		return this._desc;
	}

	public void addInput(PinElement input) {
		this._inputs.add(input);
	}

	public List<PinElement> getInputs() {
		return this._inputs;
	}

	public String toXml() {
		StringBuilder buf = new StringBuilder();

		buf.append("<task");
		if (this._name != null)
			buf.append(" name=\"").append(this._name).append("\"");
		buf.append(" class=\"").append(this._className).append("\"");
		if (this._desc != null)
			buf.append(" desc=\"").append(this._desc).append("\"");
		buf.append(">\r\n");
		for (PinElement input : this._inputs) buf.append(input.toXml());
		buf.append("</task>\r\n");

		return buf.toString();
	}
}
