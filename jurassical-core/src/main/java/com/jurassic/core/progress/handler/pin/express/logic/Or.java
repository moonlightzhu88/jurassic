package com.jurassic.core.progress.handler.pin.express.logic;

/**
 * Âß¼­»ò²Ù×÷·û
 *
 * @author yzhu
 */
public class Or extends LogicOperator {

    protected Boolean calculate(Boolean... bools) {
        return bools[0] || bools[1];
    }
}
