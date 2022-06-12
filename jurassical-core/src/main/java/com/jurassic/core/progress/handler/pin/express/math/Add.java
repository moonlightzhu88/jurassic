package com.jurassic.core.progress.handler.pin.express.math;

import java.math.BigDecimal;

/**
 * º”∑®‘ÀÀ„
 *
 * @author yzhu
 */
public class Add extends MathOperator {

    protected Integer calculate(Integer...numbers) {
        return numbers[0] + numbers[1];
    }

    protected Long calculate(Long...numbers) {
        return numbers[0] + numbers[1];
    }

    protected BigDecimal calculate(BigDecimal...numbers) {
        return numbers[0].add(numbers[1]);
    }
}
