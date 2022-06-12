package com.jurassic.core.event;

import java.lang.reflect.Method;

/**
 * ʹ�÷��䷽��ִ�е�ͨ���¼�
 *
 * @author yzhu
 */
public class ReflectionEvent extends Event{
    public static final String KEY = "reflection";

    private final Object _obj;// �����Ķ���
    private final Method _method;// ִ�еķ���
    private final Object[] _params;// ����

    public ReflectionEvent(Object obj, Method method, Object[] params) {
        this._obj = obj;
        this._method = method;
        this._params = params;
    }

    public Object getObject() {
        return this._obj;
    }

    public Method getMethod() {
        return this._method;
    }

    public Object[] getParams() {
        return this._params;
    }

    public String getPackageKey() {
        return null;
    }

    public String getEventKey() {
        return KEY;
    }
}
