package com.jurassic.core.compiler.element;

import java.util.List;

/**
 * 数据管脚标签，是配置信息中最常用的一种元素
 * 它代表了一类数据的封装
 * 流程的各个组成部分之间的信息传递都需要制定相应的数据管脚
 * 包含9种不同的管脚
 * <data/>
 * <static-object/>
 * <class/>
 * <param/>
 * <ref/>
 * <list/>
 * <express/>
 * <composite/>
 * <empty/>
 * 
 * @author yzhu
 * 
 */
public abstract class PinElement extends Element {

	/**
	 * 管脚的类型定义
	 */
	public static final int T_DATA = 0;// 数据管脚，表示一些基本类型的数据
	public static final int T_STATIC = 1;// 静态实例管脚，表示那些static的类实例对象
	public static final int T_CLASS = 2;// 类管脚，表示Class定义
	public static final int T_PARAM = 3;// 参数管脚，表示对应位置的流程输入参数
	public static final int T_REF = 4;// 引用管脚，引用其他管脚
	public static final int T_LIST = 5;// 列表管脚，由多个管脚组成的列表
	public static final int T_EXPRESS = 6;// 表达式管脚，由多个管脚和相应的表达式组成
	public static final int T_COMPOSITE = 7;// 复合管脚，由多个管脚组成的复杂数据管脚
	public static final int T_EMPTY = 8;// 空管脚，不包含数据，对应于null

	/**
	 * 获得管脚的类型
	 */
	public abstract int getPinType();

	/**
	 * 查询依赖的数据管脚
	 */
	public abstract List<String> getRefPins();

}
