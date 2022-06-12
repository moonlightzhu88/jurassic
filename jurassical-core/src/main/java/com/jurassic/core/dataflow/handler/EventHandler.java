package com.jurassic.core.dataflow.handler;

import com.jurassic.core.bus.Constant;
import com.jurassic.core.event.Event;

/**
 * ������
 * ����һ�¼��������̳߳صĹ�����ʽ
 *
 * @param <T>
 */
public abstract class EventHandler<T extends Event> extends BaseEventHandler<T> {

    public EventHandler() {
        super();
        this._numOfThread = Constant.DRPT_WORKER_SIZE;
        this._powerOfBuffer = Constant.DRPT_DATA_SIZE_POWER;
    }

    public abstract void handle(T event) throws Throwable;
}
