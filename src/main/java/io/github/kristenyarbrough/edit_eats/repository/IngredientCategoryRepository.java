package io.github.kristenyarbrough.edit_eats.repository;

import io.github.kristenyarbrough.edit_eats.domain.IngredientCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngredientCategoryRepository
        extends JpaRepository<IngredientCategory, Long> {

    Optional<IngredientCategory> findByName(String name);

    List<IngredientCategory> findByUserIsNullOrderByNameAsc();

    List<IngredientCategory> findByUserIdOrUserIsNullOrderByNameAsc(Long userId);

    boolean existsByNameAndUserIdIsNull(String name);

    boolean existsByNameAndUserId(String name, Long userId);
}
