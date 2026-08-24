package io.github.kristenyarbrough.edit_eats.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public final class QuantityFormatter {

    private static final Map<BigDecimal, String> FRACTIONS = Map.ofEntries(
            Map.entry(new BigDecimal("0.125"), "⅛"),
//            Map.entry(new BigDecimal("0.1666666667"), "⅙"),
//            Map.entry(new BigDecimal("0.2"), "⅕"),
            Map.entry(new BigDecimal("0.25"), "¼"),
            Map.entry(new BigDecimal("0.3333333333"), "⅓"),
            Map.entry(new BigDecimal("0.375"), "⅜"),
//            Map.entry(new BigDecimal("0.4"), "⅖"),
            Map.entry(new BigDecimal("0.5"), "½"),
//            Map.entry(new BigDecimal("0.6"), "⅗"),
            Map.entry(new BigDecimal("0.625"), "⅝"),
            Map.entry(new BigDecimal("0.6666666667"), "⅔"),
            Map.entry(new BigDecimal("0.75"), "¾"),
//            Map.entry(new BigDecimal("0.8"), "⅘"),
//            Map.entry(new BigDecimal("0.8333333333"), "⅚"),
            Map.entry(new BigDecimal("0.875"), "⅞")
    );

    private QuantityFormatter() {

    }

    public static String format(BigDecimal quantity) {

        if (quantity == null) {

            return "";

        }

        if (quantity.signum() == 0) {

            return "0";

        }

        boolean negative = quantity.signum() < 0;
        BigDecimal absolute = quantity.abs();

        BigDecimal whole = absolute.setScale(0, RoundingMode.FLOOR);
        BigDecimal fraction = absolute.subtract(whole);

        String fractionText = findFraction(fraction);

        String result;

        if (fractionText == null) {

            result = absolute.stripTrailingZeros().toPlainString();
            
        } else if (whole.signum() == 0) {

            result = fractionText;

        } else {

            result = whole.toPlainString() + fractionText;

        }

        return negative ? "-" + result : result;

    }

    private static String findFraction(BigDecimal fraction) {

        for (Map.Entry<BigDecimal, String> entry : FRACTIONS.entrySet()) {

            if (fraction.compareTo(entry.getKey()) == 0) {

                return entry.getValue();

            }

        }

        return null;

    }

}
