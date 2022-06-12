package com.jurassic.core.util;

public class PooledObject<V> {
    public PooledObject(V object, int index) {
        this._object = object;
        this._index = index;
    }

    private final int _index;// 对象的索引位置
    private final V _object;// 对象

    public V getObject() {
        return this._object;
    }

    public int getIndex() {
        return this._index;
    }
}
