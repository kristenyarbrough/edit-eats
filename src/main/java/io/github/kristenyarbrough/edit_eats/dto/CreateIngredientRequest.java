package io.github.kristenyarbrough.edit_eats.dto;

import io.github.kristenyarbrough.edit_eats.domain.Unit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateIngredientRequest {

    @NotBlank(message = "Ingredient name is required.")
    private String name;

    @NotNull(message = "A default unit must be selected.")
    private Unit defaultUnit;

    @NotNull(message = "An ingredient category must be selected.")
    private Long ingredientCategoryId;

}
