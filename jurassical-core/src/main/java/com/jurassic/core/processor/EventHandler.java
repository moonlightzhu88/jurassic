package com.jurassic.core.processor;

import com.jurassic.core.handler.AbstractHandler;

/**
 * ��չlmax��EventHandler
 *
 * @author yzhu
 */
public interface EventHandler<T> extends com.lmax.disruptor.EventHandler<T>{
    /**
     * ���ù����ߵĴ����߼�
     */
    void setHandler(AbstractHandler handler);
}
