package com.jurassic.core.processor;

import com.jurassic.core.handler.AbstractHandler;

/**
 * 扩展lmax的EventHandler
 *
 * @author yzhu
 */
public interface EventHandler<T> extends com.lmax.disruptor.EventHandler<T>{
    /**
     * 设置工作者的处理逻辑
     */
    void setHandler(AbstractHandler handler);
}
