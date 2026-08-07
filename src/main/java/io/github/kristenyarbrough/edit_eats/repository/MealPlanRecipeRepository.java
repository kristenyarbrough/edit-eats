package io.github.kristenyarbrough.edit_eats.repository;

import io.github.kristenyarbrough.edit_eats.domain.MealPlan;
import io.github.kristenyarbrough.edit_eats.domain.MealPlanRecipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MealPlanRecipeRepository extends JpaRepository<MealPlanRecipe, Long> {

    List<MealPlanRecipe> findByMealPlanId(Long mealPlanId);

}
