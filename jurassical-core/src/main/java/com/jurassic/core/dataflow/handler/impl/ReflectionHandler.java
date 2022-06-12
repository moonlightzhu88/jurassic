package com.jurassic.core.dataflow.handler.impl;

import com.jurassic.core.bus.impl.EBus;
import com.jurassic.core.dataflow.handler.EventHandler;
import com.jurassic.core.event.Event;
import com.jurassic.core.event.ReflectionEvent;

/**
 * 通过反射方式执行指定对象的指定方法的处理器
 * 适用于一些通用性的简单的功能处理
 * 反射处理器虽然是线程池模型，但系统默认只配置单一线程运行
 *
 * @author yzhu
 */
public class ReflectionHandler extends EventHandler<ReflectionEvent> {

    public ReflectionHandler() {
        super();
    }

    public String getHandlerKey() {
        return EBus.COMPONENT_KEY_REFLECTOR;
    }

    public Event createEvent(Object[] params) {
        return null;
    }

    public void handle(ReflectionEvent event) throws Throwable {
        // 执行反射操作
        event.getMethod().invoke(event.getObject(), event.getParams());
    }
}
