package io.github.kristenyarbrough.edit_eats.repository;

import io.github.kristenyarbrough.edit_eats.domain.RecipeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecipeCategoryRepository extends JpaRepository<RecipeCategory, Long> {

    Optional<RecipeCategory> findByUserIdAndNameIgnoreCase(
            Long userId,
            String name
    );

    List<RecipeCategory> findAllByUserIdIsNullOrUserIdOrderByNameAsc(
            Long userId
    );

}
