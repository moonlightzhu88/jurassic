package com.jurassic.core.compiler.express;

/**
 * 表达式中的变量定义
 * 表达式中的变量指的是数据管脚的定义
 * 可以是引用直接的数据管脚
 * 也可以引用某个task的输出数据管脚
 * 变量定义都以$开头
 * 如果需要引用task的输出数据管脚，则使用[index]的方式
 *
 * @author yzhu
 */
public class Variable {

    private String _refName;// 引用的数据管脚名称
    private int _index = -1;// 引用task输出数据管脚的索引位置，不适用的时候为-1

    public Variable(String refName) {
        this._refName = refName;
    }

    public Variable(String refName, int index) {
        this._refName = refName;
        this._index = index;
    }

    public String toString() {
        if (this._index == -1) {
            return "$" + this._refName;
        } else {
            return "$" + this._refName + "[" + this._index + "]";
        }
    }


    public String getRefName() {
        return this._refName;
    }

    public int getIndex() {
        return this._index;
    }
}
