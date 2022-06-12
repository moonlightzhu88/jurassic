package com.jurassic.core.progress.handler.pin.express.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * ³ý·¨ÔËËã·û
 *
 * @author yzhu
 */
public class Div extends MathOperator {

    protected Integer calculate(Integer...numbers) {
        return numbers[0] / numbers[1];
    }

    protected Long calculate(Long...numbers) {
        return numbers[0] / numbers[1];
    }

    protected BigDecimal calculate(BigDecimal...numbers) {
        return numbers[0].divide(numbers[1], RoundingMode.HALF_UP);
    }
}
