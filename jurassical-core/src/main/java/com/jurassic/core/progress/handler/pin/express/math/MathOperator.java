package com.jurassic.core.progress.handler.pin.express.math;

import com.jurassic.core.progress.handler.pin.express.DataType;
import com.jurassic.core.progress.handler.pin.express.Express;

import java.math.BigDecimal;

/**
 * 数学运算基类
 * 提供数学运算符需要的一些基本功能
 *
 * @author yzhu
 */
public abstract class MathOperator extends Express {

    /**
     * 数字类型转化
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
        // 获得操作数和最高精度的数据类型
        int maxType = -1;
        for (int i = 0; i < this._pins.length; i++) {
            datas[i] = (Number) this._pins[i].getData();
            types[i] = DataType.getType(datas[i]);
            if (types[i] > maxType)
                maxType = types[i];
        }
        // 将所有的数据转为统一类型，再进行计算
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
     * 整数运算
     */
    protected abstract Integer calculate(Integer...numbers);

    /**
     * 长整数运算
     */
    protected abstract Long calculate(Long...numbers);

    /**
     * 高精度浮点运算
     */
    protected abstract BigDecimal calculate(BigDecimal...numbers);

}
