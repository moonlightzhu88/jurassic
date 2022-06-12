package com.jurassic.core.processor;

import com.jurassic.core.handler.AbstractHandler;

/**
 * 扩展lmax的WorkHandler
 *
 * @author yzhu
 */
public interface WorkHandler<T> extends com.lmax.disruptor.WorkHandler<T>{

    /**
     * 设置工作者的处理逻辑
     */
    void setHandler(AbstractHandler handler);
}
