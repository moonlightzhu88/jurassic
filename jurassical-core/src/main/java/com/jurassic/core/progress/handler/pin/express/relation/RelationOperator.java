package com.jurassic.core.progress.handler.pin.express.relation;

import com.jurassic.core.progress.handler.pin.express.DataType;
import com.jurassic.core.progress.handler.pin.express.Express;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ��ϵ�����
 *
 * @author yzhu
 */
public abstract class RelationOperator extends Express {

    private static final Logger logger = LoggerFactory.getLogger(RelationOperator.class);

    protected Object doExpress() {
        Object[] datas = new Object[this._pins.length];
        int[] types = new int[this._pins.length];
        for (int i = 0; i < datas.length; i++) {
            datas[i] = this._pins[i].getData();
            types[i] = DataType.getType(datas[i]);
        }
        // ���������Ƿ���һ�����͵ģ������ַ��������߶�����ֵ��
        if (DataType.isNumber(types[0])) {
            for (int i = 1; i < types.length; i++) {
                if (!DataType.isNumber(types[i])) {
                    throw new RuntimeException("invalid number");
                }
            }
            Number[] numbers = new Number[datas.length];
            for (int i = 0; i < numbers.length; i++) {
                numbers[i] = (Number) datas[i];
            }
            return this.compare(numbers);
        } else if (types[0] == DataType.T_STRING) {
            for (int i = 1; i < types.length; i++) {
                if (types[i] != DataType.T_STRING) {
                    throw new RuntimeException("invalid string");
                }
            }
            String[] strings = new String[datas.length];
            for (int i = 0; i < strings.length; i++)
                strings[i] = (String) datas[i];
            return this.compare(strings);
        } else if (types[0] == DataType.T_BOOL) {
            for (int i = 1; i < types.length; i++) {
                if (types[i] != DataType.T_BOOL) {
                    throw new RuntimeException("invalid boolean");
                }
            }
            Boolean[] booleans = new Boolean[datas.length];
            for (int i = 0; i < booleans.length; i++)
                booleans[i] = (Boolean) datas[i];
            return this.compare(booleans);
        } else {
            logger.warn(datas[0].toString());
            throw new RuntimeException("invalid data");
        }
    }

    /**
     * �ַ����Ƚ�
     */
    protected abstract boolean compare(String...strings);

    /**
     * ��ֵ�Ƚ�
     */
    protected abstract boolean compare(Number...numbers);

    /**
     * ����ֵ�Ƚϣ�ֻ��==��!=���ֲ�������Ч
     */
    protected abstract boolean compare(Boolean...booleans);
}
