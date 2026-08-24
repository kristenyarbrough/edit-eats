package io.github.kristenyarbrough.edit_eats.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuantityFormatterTest {

    @Test
    void shouldFormatWholeNumber() {

        assertEquals("2", QuantityFormatter.format(new BigDecimal("2")));

    }

    @Test
    void shouldFormatQuarter() {

        assertEquals("¼", QuantityFormatter.format(new BigDecimal("0.25")));

    }

    @Test
    void shouldFormatHalf() {

        assertEquals("½", QuantityFormatter.format(new BigDecimal("0.5")));

    }

    @Test
    void shouldFormatThreeQuarters() {

        assertEquals("¾", QuantityFormatter.format(new BigDecimal("0.75")));

    }

    @Test
    void shouldFormatThirds() {
        assertEquals("⅓", QuantityFormatter.format(new BigDecimal("0.3333333333")));
        assertEquals("⅔", QuantityFormatter.format(new BigDecimal("0.6666666667")));
    }

    @Test
    void shouldFormatMixedNumber() {

        assertEquals("1¼", QuantityFormatter.format(new BigDecimal("1.25")));
        assertEquals("1½", QuantityFormatter.format(new BigDecimal("1.5")));
        assertEquals("1¾", QuantityFormatter.format(new BigDecimal("1.75")));

    }

    @Test
    void shouldFormatEighths() {

        assertEquals("⅛", QuantityFormatter.format(new BigDecimal("0.125")));
        assertEquals("⅜", QuantityFormatter.format(new BigDecimal("0.375")));
        assertEquals("⅝", QuantityFormatter.format(new BigDecimal("0.625")));
        assertEquals("⅞", QuantityFormatter.format(new BigDecimal("0.875")));

    }

    @Test
    void shouldLeaveNonFractionalDecimalAlone() {

        assertEquals("1.2", QuantityFormatter.format(new BigDecimal("1.2")));
        assertEquals("2.35", QuantityFormatter.format(new BigDecimal("2.35")));

    }

    @Test
    void shouldFormatZero() {

        assertEquals("0", QuantityFormatter.format(BigDecimal.ZERO));

    }

    @Test
    void shouldReturnEmptyStringForNull() {

        assertEquals("", QuantityFormatter.format(null));

    }

}
