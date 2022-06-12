package com.jurassic.core.progress.handler.pin.express.logic;

import com.jurassic.core.progress.handler.pin.express.Express;

/**
 * Âß¼­ÔËËã·û
 *
 * @author yzhu
 */
public abstract class LogicOperator extends Express {

    protected Object doExpress() {
        Boolean[] bools = new Boolean[this._pins.length];
        for (int i = 0; i < bools.length; i++) {
            bools[i] = (Boolean) this._pins[i].getData();
        }
        return this.calculate(bools);
    }

    protected abstract Boolean calculate(Boolean...bools);
}
