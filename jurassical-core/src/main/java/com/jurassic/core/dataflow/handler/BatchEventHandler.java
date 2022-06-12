package com.jurassic.core.dataflow.handler;

import com.jurassic.core.bus.Constant;
import com.jurassic.core.event.Event;

import java.util.List;

/**
 * 处理器
 * 一次处理多个事件，采用单线程处理方式
 *
 * @author yzhu
 */
public abstract class BatchEventHandler<T extends Event> extends BaseEventHandler<T> {
    public BatchEventHandler() {
        super();
        this._numOfThread = 1;
        this._powerOfBuffer = Constant.DRPT_DATA_SIZE_POWER;
    }

    public abstract void handle(List<T> events) throws Throwable;
}
