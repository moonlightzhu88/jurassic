package com.jurassic.core.handler;

/**
 * Handler����
 *
 * @author yzhu
 */
public class HandlerConfig {

    private final int _numOfThread;// �߳���
    private final int _powerOfBuffer;// ��������С

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
