package io.github.kristenyarbrough.edit_eats.dto.request;

import io.github.kristenyarbrough.edit_eats.domain.MealPlan;
import io.github.kristenyarbrough.edit_eats.domain.MealType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AddMealPlanRecipeRequest {

    @NotNull
    private Long recipeId;

    @NotNull
    private LocalDate mealDate;

    @NotNull
    private MealType mealType;

    @NotNull
    @Min(1)
    private Integer servings;

}
