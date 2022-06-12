package com.jurassic.core.progress.handler.pin.express.logic;

/**
 * Âß¼­·ÇÔËËã
 *
 * @author yzhu
 */
public class Not extends LogicOperator {

    protected Boolean calculate(Boolean... bools) {
        return !bools[0];
    }
}
