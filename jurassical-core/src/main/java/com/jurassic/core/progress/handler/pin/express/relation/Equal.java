package com.jurassic.core.progress.handler.pin.express.relation;

/**
 * µÈÓÚ²Ù×÷·û
 *
 * @author yzhu
 */
public class Equal extends RelationOperator {

    protected boolean compare(String... strings) {
        return strings[0].equals(strings[1]);
    }

    protected boolean compare(Number... numbers) {
        return numbers[0].doubleValue() == numbers[1].doubleValue();
    }

    protected boolean compare(Boolean... booleans) {
        return booleans[0] == booleans[1];
    }
}
