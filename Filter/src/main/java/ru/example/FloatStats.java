package ru.example;

import java.math.BigDecimal;
import java.math.MathContext;

public class FloatStats {
    long count = 0;
    BigDecimal min = null;
    BigDecimal max = null;
    BigDecimal sum = BigDecimal.ZERO;

    public void add(BigDecimal val) {
        count++;
        if (min == null || val.compareTo(min) < 0) {
            min = val;
        }
        if (max == null || val.compareTo(max) > 0) {
            max = val;
        }
        sum = sum.add(val);
    }

    public BigDecimal getAverage() {
        if (count == 0) {
            return BigDecimal.ZERO;
        }
        return sum.divide(BigDecimal.valueOf(count), MathContext.DECIMAL64);
    }
}