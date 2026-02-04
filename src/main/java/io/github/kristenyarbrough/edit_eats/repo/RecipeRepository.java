package io.github.kristenyarbrough.edit_eats.repo;

import io.github.kristenyarbrough.edit_eats.domain.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findAllByOrderByCreatedAtDesc();
    List<Recipe> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
