package io.github.kristenyarbrough.edit_eats.dto.request;

import io.github.kristenyarbrough.edit_eats.constants.ValidationConstants;
import io.github.kristenyarbrough.edit_eats.domain.Unit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateRecipeIngredientRequest {

    @NotNull
    private Long ingredientId;

    @NotNull
    @DecimalMin(value = ValidationConstants.MIN_RECIPE_QUANTITY)
    private BigDecimal quantity;

    @NotNull
    private Unit unit;

    private String preparation;

    private Boolean optional = false;

}
