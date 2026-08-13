package io.github.kristenyarbrough.edit_eats.repository;

import io.github.kristenyarbrough.edit_eats.domain.RecipeStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeStepRepository extends JpaRepository<RecipeStep, Long> {

    List<RecipeStep> findByRecipeIdOrderByStepNumber(Long recipeId);

    long countByRecipeId(Long recipeId);

    void deleteByRecipeId(Long recipeId);

}
