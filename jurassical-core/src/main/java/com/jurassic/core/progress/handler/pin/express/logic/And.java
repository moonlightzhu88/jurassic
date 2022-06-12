package com.jurassic.core.progress.handler.pin.express.logic;

/**
 * Âß¼­Óë²Ù×÷·û
 *
 * @author yzhu
 */
public class And extends LogicOperator {

    protected Boolean calculate(Boolean... bools) {
        return bools[0] && bools[1];
    }
}
