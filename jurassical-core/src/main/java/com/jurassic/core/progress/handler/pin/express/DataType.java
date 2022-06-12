package com.jurassic.core.progress.handler.pin.express;

import java.math.BigDecimal;

/**
 * ��������
 *
 * @author yzhu
 */
public class DataType {

    public static final int T_INT = 1;
    public static final int T_LONG = 2;
    public static final int T_DECIMAL = 3;
    public static final int T_STRING = 4;
    public static final int T_BOOL = 5;

    /**
     * ������ֵ����ͣ����Σ������ͣ��߾��ȸ�����)
     */
    public static int getType(Object data) {
        if (data instanceof Integer)
            return T_INT;
        else if (data instanceof Long)
            return T_LONG;
        else if (data instanceof BigDecimal)
            return T_DECIMAL;
        else if (data instanceof String)
            return T_STRING;
        else if (data instanceof Boolean)
            return T_BOOL;
        else
            return -1;
    }

    /**
     * �ж������Ƿ�����ֵ
     */
    public static boolean isNumber(int type) {
        return type == T_INT || type == T_LONG || type == T_DECIMAL;
    }

}
