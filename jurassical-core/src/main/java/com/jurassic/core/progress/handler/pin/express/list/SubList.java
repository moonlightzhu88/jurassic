package com.jurassic.core.progress.handler.pin.express.list;

import com.jurassic.core.progress.handler.pin.express.Express;

import java.util.List;

/**
 * ������б�
 *
 * @author yzhu
 */
public class SubList extends Express {

    protected Object doExpress() {
        List list = (List)this._pins[0].getData();// ��ȡ���б�
        int from = (Integer)this._pins[1].getData();// ��ʼλ��
        int to = (Integer)this._pins[2].getData();// ����λ��
        return list.subList(from, to);
    }
}
