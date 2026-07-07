package io.github.kristenyarbrough.edit_eats.dto.response;

import io.github.kristenyarbrough.edit_eats.domain.Unit;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RecipeIngredientResponse {

    private Long ingredientId;
    private String ingredientName;

    private BigDecimal quantity;
    private Unit unit;

    private String preparation;
    private Boolean optional;

}
