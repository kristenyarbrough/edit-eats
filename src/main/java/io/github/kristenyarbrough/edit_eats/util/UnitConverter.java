package io.github.kristenyarbrough.edit_eats.util;

import io.github.kristenyarbrough.edit_eats.domain.Unit;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class UnitConverter {

    public static BigDecimal convert(BigDecimal qty, Unit from, Unit to) {
        if (from == to) return qty;

        // weight conversions
        if ((from == Unit.G && to == Unit.KG)) {
            return qty.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
        }
        if ((from == Unit.KG && to == Unit.G)) {
            return qty.multiply(BigDecimal.valueOf(1000));
        }

        // volume conversions
        if (from == Unit.ML && to == Unit.L) {
            return qty.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
        }
        if (from == Unit.L && to == Unit.ML) {
            return qty.multiply(BigDecimal.valueOf(1000));
        }

        // tbsp ↔ tsp
        if (from == Unit.TBSP && to == Unit.TSP) {
            return qty.multiply(BigDecimal.valueOf(3));
        }
        if (from == Unit.TSP && to == Unit.TBSP) {
            return qty.divide(BigDecimal.valueOf(3), 3, RoundingMode.HALF_UP);
        }

        // cup ↔ ml (approx cooking standard)
        if (from == Unit.CUP && to == Unit.ML) {
            return qty.multiply(BigDecimal.valueOf(250));
        }
        if (from == Unit.ML && to == Unit.CUP) {
            return qty.divide(BigDecimal.valueOf(250), 3, RoundingMode.HALF_UP);
        }

        throw new IllegalArgumentException("Unsupported conversion: " + from + " to " + to);
    }
}
