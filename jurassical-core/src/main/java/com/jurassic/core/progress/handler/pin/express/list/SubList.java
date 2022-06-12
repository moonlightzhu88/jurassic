package com.jurassic.core.progress.handler.pin.express.list;

import com.jurassic.core.progress.handler.pin.express.Express;

import java.util.List;

/**
 * 获得子列表
 *
 * @author yzhu
 */
public class SubList extends Express {

    protected Object doExpress() {
        List list = (List)this._pins[0].getData();// 截取的列表
        int from = (Integer)this._pins[1].getData();// 开始位置
        int to = (Integer)this._pins[2].getData();// 结束位置
        return list.subList(from, to);
    }
}
