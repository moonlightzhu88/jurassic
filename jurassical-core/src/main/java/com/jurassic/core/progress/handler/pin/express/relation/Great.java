package com.jurassic.core.progress.handler.pin.express.relation;

/**
 * ���ڹ�ϵ������
 *
 * @author yzhu
 */
public class Great extends RelationOperator {

    protected boolean compare(String... strings) {
        return strings[0].compareTo(strings[1]) > 0;
    }


    protected boolean compare(Number... numbers) {
        return numbers[0].doubleValue() > numbers[1].doubleValue();
    }

    protected boolean compare(Boolean... booleans) {
        throw new RuntimeException("invalid operator");
    }

}
