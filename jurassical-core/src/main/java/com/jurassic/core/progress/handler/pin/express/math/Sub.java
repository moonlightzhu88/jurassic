package com.jurassic.core.progress.handler.pin.express.math;

import java.math.BigDecimal;

/**
 * ºı∑®‘ÀÀ„
 *
 * @author yzhu
 */
public class Sub extends MathOperator {

    protected Integer calculate(Integer...numbers) {
        return numbers[0] - numbers[1];
    }

    protected Long calculate(Long...numbers) {
        return numbers[0] - numbers[1];
    }

    protected BigDecimal calculate(BigDecimal...numbers) {
        return numbers[0].subtract(numbers[1]);
    }}
