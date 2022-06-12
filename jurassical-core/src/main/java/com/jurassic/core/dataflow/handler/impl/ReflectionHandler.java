package com.jurassic.core.dataflow.handler.impl;

import com.jurassic.core.bus.impl.EBus;
import com.jurassic.core.dataflow.handler.EventHandler;
import com.jurassic.core.event.Event;
import com.jurassic.core.event.ReflectionEvent;

/**
 * ͨ�����䷽ʽִ��ָ�������ָ�������Ĵ�����
 * ������һЩͨ���Եļ򵥵Ĺ��ܴ���
 * ���䴦������Ȼ���̳߳�ģ�ͣ���ϵͳĬ��ֻ���õ�һ�߳�����
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
        // ִ�з������
        event.getMethod().invoke(event.getObject(), event.getParams());
    }
}
