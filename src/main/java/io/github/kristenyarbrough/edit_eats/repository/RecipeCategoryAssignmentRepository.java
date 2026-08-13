package io.github.kristenyarbrough.edit_eats.repository;

import io.github.kristenyarbrough.edit_eats.domain.RecipeCategoryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeCategoryAssignmentRepository extends JpaRepository<RecipeCategoryAssignment, Long> {

    List<RecipeCategoryAssignment> findByRecipeId(Long recipeId);

    void deleteByRecipeId(Long recipeId);

}
