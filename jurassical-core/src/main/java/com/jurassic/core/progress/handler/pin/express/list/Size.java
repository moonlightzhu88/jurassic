package com.jurassic.core.progress.handler.pin.express.list;

import com.jurassic.core.progress.handler.pin.express.Express;

import java.util.List;

/**
 * ����б��Ԫ�ظ���
 *
 * @author yzhu
 */
public class Size extends Express {

    protected Object doExpress() {
        List data = (List)this._pins[0].getData();
        return data.size();
    }
}
