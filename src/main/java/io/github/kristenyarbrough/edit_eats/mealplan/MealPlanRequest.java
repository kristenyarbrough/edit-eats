package io.github.kristenyarbrough.edit_eats.mealplan;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MealPlanRequest {
    private LocalDate weekStarting;
    private List<Long> recipeIds;
}
