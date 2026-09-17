package io.github.kristenyarbrough.edit_eats.repository;

import io.github.kristenyarbrough.edit_eats.domain.RecipeInstructionSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeInstructionSectionRepository extends JpaRepository<RecipeInstructionSection, Long> {

    List<RecipeInstructionSection> findByRecipeIdOrderBySortOrder(Long recipeId);

    void deleteByRecipeId(Long recipeId);

}
