package com.jurassic.core.handler;

/**
 * Handler配置
 *
 * @author yzhu
 */
public class HandlerConfig {

    private final int _numOfThread;// 线程数
    private final int _powerOfBuffer;// 缓冲区大小

    public HandlerConfig(int numOfThread, int powerOfBuffer) {
        this._numOfThread = numOfThread;
        this._powerOfBuffer = powerOfBuffer;
    }

    public int getNumOfThread() {
        return this._numOfThread;
    }

    public int getPowerOfBuffer() {
        return this._powerOfBuffer;
    }

}
