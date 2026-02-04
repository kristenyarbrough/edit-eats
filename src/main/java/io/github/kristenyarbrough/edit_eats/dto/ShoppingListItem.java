package io.github.kristenyarbrough.edit_eats.dto;

import io.github.kristenyarbrough.edit_eats.domain.Unit;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ShoppingListItem {
    private String ingredient;
    private BigDecimal quantity;
    private Unit unit;
}
