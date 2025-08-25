package ru.example;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class IntStats {
    long count = 0;
    BigInteger min = null;
    BigInteger max = null;
    BigInteger sum = BigInteger.ZERO;

    public void add(BigInteger val) {
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
        return new BigDecimal(sum).divide(BigDecimal.valueOf(count), MathContext.DECIMAL64);
    }
}