package com.jurassic.core.event;

import java.lang.reflect.Method;

/**
 * 使用反射方法执行的通用事件
 *
 * @author yzhu
 */
public class ReflectionEvent extends Event{
    public static final String KEY = "reflection";

    private final Object _obj;// 触发的对象
    private final Method _method;// 执行的方法
    private final Object[] _params;// 参数

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
