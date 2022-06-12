package com.jurassic.core.progress.handler.pin.express.date;

import com.jurassic.core.progress.handler.pin.express.Express;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期格式化
 *
 * @author yzhu
 */
public class DateFormat extends Express {

    protected Object doExpress() {
        Date date = (Date) this._pins[0].getData();
        String format = (String) this._pins[1].getData();
        // 日期格式化
        return new SimpleDateFormat(format).format(date);
    }
}
