package com.jurassic.core.util;

public class PooledObject<V> {
    public PooledObject(V object, int index) {
        this._object = object;
        this._index = index;
    }

    private final int _index;// ���������λ��
    private final V _object;// ����

    public V getObject() {
        return this._object;
    }

    public int getIndex() {
        return this._index;
    }
}
