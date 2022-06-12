package com.jurassic.core.progress.handler.pin;

import java.util.ArrayList;
import java.util.List;

/**
 * �������ݹܽ�
 * �ɶ�����������ݹܽ����
 *
 * @author yzhu
 */
public abstract class CompositePin extends Pin{

    // ���ݹܽŶ����keyΪ�����ܽŵ�����
    protected final List<Pin> _pins = new ArrayList<>();

    public void addPin(Pin pin) {
        this._pins.add(pin);
    }

    public Object getData() {
        Object[] datas = new Object[this._pins.size()];
        for (int i = 0; i < datas.length; i++) {
            datas[i] = this._pins.get(i).getData();
        }
        return datas;
    }
}
