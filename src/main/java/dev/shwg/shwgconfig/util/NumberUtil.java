package dev.shwg.shwgconfig.util;

import java.math.BigDecimal;

public final class NumberUtil {

    private NumberUtil() {
    }

    /**
     * Converts a number to an exact decimal without picking up floating point noise.
     * {@code Float.toString} / {@code Double.toString} print the shortest decimal that
     * turns back into exactly that number, so {@code 1.2f} becomes {@code 1.2} - not
     * {@code 1.2000000476837158}, which is what converting the float to a double gives.
     *
     * @throws NumberFormatException for NaN or infinity, which have no decimal form
     */
    public static BigDecimal toDecimal(Number number) {
        return new BigDecimal(number.toString());
    }
}
