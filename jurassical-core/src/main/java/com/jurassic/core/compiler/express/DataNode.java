package com.jurassic.core.compiler.express;

/**
 * 数据节点
 * 可以表示常量（字符串，布尔值，数值）
 * 也可以表示变量（数据管脚），以$开头
 *
 * @author yzhu
 */
public class DataNode extends Node{

    private Object _data;// 数据，为String，Number，Boolean，Variable4类

    public DataNode(Object data) {
        this._data = data;
    }

    public Object getData() {
        return this._data;
    }

    public String toText() {
        if (this._data instanceof String)
            return "'" + (String) this._data + "'";
        else
            return this._data.toString();
    }
}
