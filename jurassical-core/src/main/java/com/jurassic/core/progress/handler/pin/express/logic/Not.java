package com.jurassic.core.progress.handler.pin.express.logic;

/**
 * �߼�������
 *
 * @author yzhu
 */
public class Not extends LogicOperator {

    protected Boolean calculate(Boolean... bools) {
        return !bools[0];
    }
}
