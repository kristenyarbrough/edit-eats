package io.github.kristenyarbrough.edit_eats.repository;

import io.github.kristenyarbrough.edit_eats.domain.IngredientCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IngredientCategoryRepository
        extends JpaRepository<IngredientCategory, Long> {

    Optional<IngredientCategory> findByName(String name);
}
