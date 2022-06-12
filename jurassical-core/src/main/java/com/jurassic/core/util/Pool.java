package com.jurassic.core.util;

/**
 * 对象池
 */
public class Pool<V> extends MallocedArray{

    private final Class<V> _clz;

    public Pool(boolean safe, int batchSize, Class<V> clz) {
        super(safe, batchSize);
        this._clz = clz;
        for (int i = 0; i < batchSize; i++) {
            this._array[i] = this.createObject(i);
        }
    }

    protected Object createObject(int index) {
        try {
            V object =  this._clz.newInstance();
            return new PooledObject<>(object, index);
        } catch (Throwable ex) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public PooledObject<V> mallocPooledObj() {
        this.lock();
        try {
            // 分配一个存储空间
            int index = super.malloc();
            return (PooledObject<V>) this._array[index];
        } finally {
            this.unlock();
        }
    }

    public void freePooledObj(PooledObject<V> pooledObject) {
        this.lock();
        try {
            this.free(pooledObject.getIndex());
        } finally {
            this.unlock();
        }
    }
}
