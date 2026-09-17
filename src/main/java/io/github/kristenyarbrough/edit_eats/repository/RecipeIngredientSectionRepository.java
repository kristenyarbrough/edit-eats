package io.github.kristenyarbrough.edit_eats.repository;

import io.github.kristenyarbrough.edit_eats.domain.RecipeIngredientSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeIngredientSectionRepository extends JpaRepository<RecipeIngredientSection, Long> {

    List<RecipeIngredientSection> findByRecipeIdOrderBySortOrder(Long recipeId);

    void deleteByRecipeId(Long recipeId);

}
