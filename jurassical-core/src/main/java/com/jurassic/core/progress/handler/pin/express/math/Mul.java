package com.jurassic.core.progress.handler.pin.express.math;

import java.math.BigDecimal;

/**
 * ³Ë·¨ÔËËã·û
 *
 * @author yzhu
 */
public class Mul extends MathOperator {

    protected Integer calculate(Integer...numbers) {
        return numbers[0] * numbers[1];
    }

    protected Long calculate(Long...numbers) {
        return numbers[0] * numbers[1];
    }

    protected BigDecimal calculate(BigDecimal...numbers) {
        return numbers[0].multiply(numbers[1]);
    }
}
