package com.jurassic.core.progress.handler.pin;

/**
 * ���ݹܽŵľ��
 * ����lazy���صĳ���
 *
 * @author yzhu
 */
public class PinHandler extends Pin{

    private Pin _pin;// ʵ�ʵĹܽ�

    public void setPin(Pin pin) {
        this._pin = pin;
    }

    public Object getData() {
        return this._pin.getData();
    }
}
