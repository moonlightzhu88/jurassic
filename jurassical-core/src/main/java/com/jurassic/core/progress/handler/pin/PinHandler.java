package com.jurassic.core.progress.handler.pin;

/**
 * 数据管脚的句柄
 * 用于lazy加载的场景
 *
 * @author yzhu
 */
public class PinHandler extends Pin{

    private Pin _pin;// 实际的管脚

    public void setPin(Pin pin) {
        this._pin = pin;
    }

    public Object getData() {
        return this._pin.getData();
    }
}
