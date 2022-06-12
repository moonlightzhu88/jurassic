package com.jurassic.core.progress.handler.pin.express.relation;

/**
 * 不等于操作符
 *
 * @author yzhu
 */
public class NotEqual extends RelationOperator {

    protected boolean compare(String... strings) {
        return !strings[0].equals(strings[1]);
    }


    protected boolean compare(Number... numbers) {
        return numbers[0].doubleValue() != numbers[1].doubleValue();
    }

    protected boolean compare(Boolean... booleans) {
        return booleans[0] != booleans[1];
    }
}
