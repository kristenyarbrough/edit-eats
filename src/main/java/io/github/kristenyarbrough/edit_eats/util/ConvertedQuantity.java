package io.github.kristenyarbrough.edit_eats.util;

import io.github.kristenyarbrough.edit_eats.domain.Unit;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record ConvertedQuantity(BigDecimal quantity, Unit unit) {

}
