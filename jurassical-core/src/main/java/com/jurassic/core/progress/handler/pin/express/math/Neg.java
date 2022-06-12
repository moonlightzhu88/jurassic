package com.jurassic.core.progress.handler.pin.express.math;

import java.math.BigDecimal;

/**
 * ¸ºÊıÔËËã
 *
 * @author yzhu
 */
public class Neg extends MathOperator {

    protected Integer calculate(Integer...numbers) {
        return -numbers[0];
    }

    protected Long calculate(Long...numbers) {
        return -numbers[0];
    }

    protected BigDecimal calculate(BigDecimal...numbers) {
        return numbers[0].negate();
    }
}
