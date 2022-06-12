package com.jurassic.core.progress.handler.pin.express.list;

import com.jurassic.core.progress.handler.pin.express.Express;

import java.util.List;

/**
 * 选取列表的元素
 *
 * @author yzhu
 */
public class Element extends Express {

    protected Object doExpress() {
       List list = (List) this._pins[0].getData();
       int index = (Integer) this._pins[1].getData();
       return list.get(index);
    }
}
