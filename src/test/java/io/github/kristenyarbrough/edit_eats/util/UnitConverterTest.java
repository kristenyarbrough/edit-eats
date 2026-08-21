package io.github.kristenyarbrough.edit_eats.util;


import io.github.kristenyarbrough.edit_eats.domain.Unit;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnitConverterTest {

    @Test
    void shouldConvertMillilitresToLitresWhenQuantityReachesOneLitre() {

        ConvertedQuantity result =
                UnitConverter.normalise(
                        new BigDecimal("1600"),
                        Unit.ML
                );

        assertEquals(0,
                new BigDecimal("1.6").compareTo(result.quantity()));
        assertEquals(Unit.L, result.unit());

    }

    @Test
    void shouldKeepMillilitresWhenQuantityIsLessThanOneLitre() {

        ConvertedQuantity result =
                UnitConverter.normalise(
                        new BigDecimal("800"),
                        Unit.ML
                );

        assertEquals(new BigDecimal("800"), result.quantity());
        assertEquals(Unit.ML, result.unit());
    }

    @Test
    void shouldConvertGramsToKilogramsWhenQuantityReachesOneKilogram() {

        ConvertedQuantity result =
                UnitConverter.normalise(
                        new BigDecimal("1500"),
                        Unit.G
                );

        assertEquals(0,
                new BigDecimal("1.5").compareTo(result.quantity()));
        assertEquals(Unit.KG, result.unit());

    }

    @Test
    void shouldKeepGramsWhenQuantityIsLessThanOneKilogram() {

        ConvertedQuantity result =
                UnitConverter.normalise(
                        new BigDecimal("800"),
                        Unit.G
                );

        assertEquals(new BigDecimal("800"), result.quantity());
        assertEquals(Unit.G, result.unit());

    }

}
