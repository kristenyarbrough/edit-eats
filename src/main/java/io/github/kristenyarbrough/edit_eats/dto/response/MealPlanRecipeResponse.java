package io.github.kristenyarbrough.edit_eats.dto.response;

import io.github.kristenyarbrough.edit_eats.domain.MealType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class MealPlanRecipeResponse {

    private Long id;
    private Long recipeId;
    private String recipeName;
    private LocalDate mealDate;
    private MealType mealType;
    private Integer servings;

}
