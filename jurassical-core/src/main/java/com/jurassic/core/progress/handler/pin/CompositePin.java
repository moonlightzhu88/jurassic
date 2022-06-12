package com.jurassic.core.progress.handler.pin;

import java.util.ArrayList;
import java.util.List;

/**
 * 复合数据管脚
 * 由多个独立的数据管脚组成
 *
 * @author yzhu
 */
public abstract class CompositePin extends Pin{

    // 数据管脚定义表，key为各个管脚的名称
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
