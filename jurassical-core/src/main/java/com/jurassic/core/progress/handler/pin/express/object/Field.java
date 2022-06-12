package com.jurassic.core.progress.handler.pin.express.object;

import com.jurassic.core.progress.handler.pin.express.Express;

/**
 * 获取对象属性
 *
 * @author yzhu
 */
public class Field extends Express {

    protected Object doExpress() {
        Object object = this._pins[0].getData();
        String nameOfField = (String) this._pins[1].getData();
        try {
            // 反射方法获得对象属性
            java.lang.reflect.Field field =
                    object.getClass().getDeclaredField(nameOfField);
            boolean access = field.isAccessible();
            field.setAccessible(true);
            Object data = field.get(object);
            field.setAccessible(access);
            return data;
        } catch (Throwable ex) {
            throw new RuntimeException("invalid field");
        }
    }
}
