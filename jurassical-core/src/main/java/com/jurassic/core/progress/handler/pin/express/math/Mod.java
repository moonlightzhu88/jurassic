package com.jurassic.core.progress.handler.pin.express.math;

import java.math.BigDecimal;

/**
 * «Ûƒ£‘ÀÀ„
 *
 * @author yzhu
 */
public class Mod extends MathOperator {

    protected Integer calculate(Integer...numbers) {
        return numbers[0] % numbers[1];
    }

    protected Long calculate(Long...numbers) {
        return numbers[0] % numbers[1];
    }

    protected BigDecimal calculate(BigDecimal...numbers) {
        throw new RuntimeException("invalid number");
    }
}
