package com.jurassic.core.compiler.element;

/**
 * 结果通知标签
 * <notification task="...">
 *     <pin .../>
 * </notification>
 * 
 * @author yzhu
 * 
 */
public class NotificationElement extends Element {

	private String _task;// 流程结束的任务名称
	private String _classOfNotification;// 通知接口类名
	private PinElement _result;// 流程的输出数据管脚

	public void setTask(String task) {
		this._task = task;
	}

	public String getTask() {
		return this._task;
	}

	public void setClassOfNotification(String className) {
		this._classOfNotification = className;
	}

	public String getClassOfNotification() {
		return this._classOfNotification;
	}

	public void setResult(PinElement result) {
		this._result = result;
	}

	public PinElement getResult() {
		return this._result;
	}

	public String toXml() {
		StringBuilder buf = new StringBuilder();

		buf.append("<notification");
		if (this._classOfNotification != null) {
			buf.append(" class=\"").append(this._classOfNotification)
					.append("\"");
		}
		buf.append(" task=\"").append(this._task)
				.append("\">\r\n");
		if (this._result != null)
			buf.append(this._result.toXml());
		buf.append("</notification>\r\n");

		return buf.toString();
	}

}
