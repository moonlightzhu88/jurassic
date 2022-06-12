package com.jurassic.core.progress.handler.pin.express.object;

import com.jurassic.core.progress.handler.pin.express.Express;

/**
 * ��ȡ��������
 *
 * @author yzhu
 */
public class Field extends Express {

    protected Object doExpress() {
        Object object = this._pins[0].getData();
        String nameOfField = (String) this._pins[1].getData();
        try {
            // ���䷽����ö�������
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
