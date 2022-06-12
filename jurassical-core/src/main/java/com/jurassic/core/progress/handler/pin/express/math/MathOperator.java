package com.jurassic.core.progress.handler.pin.express.math;

import com.jurassic.core.progress.handler.pin.express.DataType;
import com.jurassic.core.progress.handler.pin.express.Express;

import java.math.BigDecimal;

/**
 * ��ѧ�������
 * �ṩ��ѧ�������Ҫ��һЩ��������
 *
 * @author yzhu
 */
public abstract class MathOperator extends Express {

    /**
     * ��������ת��
     */
    @SuppressWarnings("unchecked")
    private <T> T castNumber(Object data, Class<T> clz){
        if (clz.equals(Integer.class)) {
            if (data instanceof Integer) {
                return (T)data;
            } else if (data instanceof Long) {
                Object tmp = ((Long) data).intValue();
                return (T)tmp;
            } else if (data instanceof BigDecimal) {
                Object tmp = ((BigDecimal) data).intValue();
                return (T) tmp;
            }
        } else if (clz.equals(Long.class)) {
            if (data instanceof Integer) {
                Object tmp = (long)((Integer) data);
                return (T)tmp;
            } else if (data instanceof Long) {
                return (T) data;
            } else if (data instanceof BigDecimal) {
                Object tmp = ((BigDecimal) data).longValue();
                return (T) tmp;
            }
        } else if (clz.equals(BigDecimal.class)) {
            if (data instanceof Integer) {
                Object tmp = new BigDecimal((Integer) data);
                return (T)tmp;
            } else if (data instanceof Long) {
                Object tmp = new BigDecimal((Long) data);
                return (T)tmp;
            } else if (data instanceof BigDecimal) {
                return (T) data;
            }
        }
        throw new RuntimeException("invalid number");
    }

    protected Object doExpress() {
        Number[] datas = new Number[this._pins.length];
        int[] types = new int[this._pins.length];
        // ��ò���������߾��ȵ���������
        int maxType = -1;
        for (int i = 0; i < this._pins.length; i++) {
            datas[i] = (Number) this._pins[i].getData();
            types[i] = DataType.getType(datas[i]);
            if (types[i] > maxType)
                maxType = types[i];
        }
        // �����е�����תΪͳһ���ͣ��ٽ��м���
        if (maxType == DataType.T_INT) {
            Integer[] numbers = new Integer[this._pins.length];
            for (int i = 0; i < numbers.length; i++)
                numbers[i] = this.castNumber(datas[i], Integer.class);
            return this.calculate(numbers);
        } else if (maxType == DataType.T_LONG) {
            Long[] numbers = new Long[this._pins.length];
            for (int i = 0; i < numbers.length; i++)
                numbers[i] = this.castNumber(datas[i], Long.class);
            return this.calculate(numbers);
        } else if (maxType == DataType.T_DECIMAL){
            BigDecimal[] numbers = new BigDecimal[this._pins.length];
            for (int i = 0; i < numbers.length; i++)
                numbers[i] = this.castNumber(datas[i], BigDecimal.class);
            return this.calculate(numbers);
        }
        throw new RuntimeException("inalid number");
    }

    /**
     * ��������
     */
    protected abstract Integer calculate(Integer...numbers);

    /**
     * ����������
     */
    protected abstract Long calculate(Long...numbers);

    /**
     * �߾��ȸ�������
     */
    protected abstract BigDecimal calculate(BigDecimal...numbers);

}
