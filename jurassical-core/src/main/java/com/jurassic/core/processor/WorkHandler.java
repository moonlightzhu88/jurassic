package com.jurassic.core.processor;

import com.jurassic.core.handler.AbstractHandler;

/**
 * ��չlmax��WorkHandler
 *
 * @author yzhu
 */
public interface WorkHandler<T> extends com.lmax.disruptor.WorkHandler<T>{

    /**
     * ���ù����ߵĴ����߼�
     */
    void setHandler(AbstractHandler handler);
}
