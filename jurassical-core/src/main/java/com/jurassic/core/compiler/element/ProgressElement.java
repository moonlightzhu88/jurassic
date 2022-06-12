package com.jurassic.core.compiler.element;

import java.util.*;

/**
 * 流程定义的顶层元素
 * <progress name="...">
 *     	<input .../>
 *     	<output .../>
 *     	<pin .../>
 *     	<task .../>
 *     	<except-table .../>
 *     	<binds .../>
 *     	<notification .../>
 * </progress>
 * 
 * @author yzhu
 * 
 */
public class ProgressElement extends Element {

	private List<ProgressParamElement> _inputs = new ArrayList<>();// 流程输入标签
	private List<ProgressParamElement> _outputs = new ArrayList<>();// 流程输出标签
	// 数据管脚标签，不包含那些定义在元素内部的匿名Pin
	private final Map<String, PinElement> _pins = new HashMap<>();
	private final Map<String, TaskElement> _tasks = new HashMap<>();// 任务标签
	// 子流程
	private final Map<String, SubProgressElement> _subProgress = new HashMap<>();
	private BindsElement _binding;// 流程连接
	private NotificationElement _notification;// 结果通知
	private ExceptionTableElement _exceptTable;// 异常表

	public void addInput(ProgressParamElement input) {
		this._inputs.add(input);
	}

	public List<ProgressParamElement> getInputs() {
		return this._inputs;
	}

	public void addOutput(ProgressParamElement output) {
		this._outputs.add(output);
	}

	public List<ProgressParamElement> getOutputs() {
		return this._outputs;
	}

	public void addPin(PinElement pin) {
		this._pins.put(pin.getName(), pin);
	}

	public Map<String, PinElement> getPins() {
		return this._pins;
	}

	public void addTask(TaskElement task) {
		this._tasks.put(task.getName(), task);
	}

	public Map<String, TaskElement> getTasks() {
		return this._tasks;
	}

	public void addSubProgress(SubProgressElement subProgress) {
		this._subProgress.put(subProgress.getName(), subProgress);
	}

	public Map<String, SubProgressElement> getSubProgress() {
		return this._subProgress;
	}

	public void setExceptTable(ExceptionTableElement table) {
		this._exceptTable = table;
	}

	public ExceptionTableElement getExceptTable() {
		return this._exceptTable;
	}

	public void setBinding(BindsElement binding) {
		this._binding = binding;
	}

	public BindsElement getBinds() {
		return this._binding;
	}

	public void setNotification(NotificationElement notification) {
		this._notification = notification;
	}

	public NotificationElement getNotification() {
		return this._notification;
	}

	public String toXml() {
		StringBuilder buf = new StringBuilder();

		buf.append("<progress name=\"").append(this._name).append("\">\r\n");
		for (int i = 0; i < this._inputs.size(); i++)
			buf.append(this._inputs.get(i).toXml());
		for (int i = 0; i < this._outputs.size(); i++)
			buf.append(this._outputs.get(i).toXml());
		for (Map.Entry<String, PinElement> entry : this._pins.entrySet()) {
			buf.append(entry.getValue().toXml());
		}
		for (Map.Entry<String, TaskElement> entry : this._tasks.entrySet()) {
			buf.append(entry.getValue().toXml());
		}
		if (this._exceptTable != null)
			buf.append(this._exceptTable.toXml());
		buf.append(this._binding.toXml());
		if (this._notification != null)
			buf.append(this._notification.toXml());
		buf.append("</progress>\r\n");

		return buf.toString();
	}

}
