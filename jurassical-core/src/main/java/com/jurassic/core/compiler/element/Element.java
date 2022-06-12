package com.jurassic.core.compiler.element;

/**
 * 流程定义的基础抽象元素定义
 * 
 * @author yzhu
 * 
 */
public abstract class Element {

	// 标签的名称，名称通常作为在配置信息中的一个引用信息
	// name并不是必须的属性
	protected String _name;

	public void setName(String name) {
		this._name = name;
	}

	public String getName() {
		return this._name;
	}

	public abstract String toXml();

}
