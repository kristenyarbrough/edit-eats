package io.github.kristenyarbrough.edit_eats.repository;

import io.github.kristenyarbrough.edit_eats.domain.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    Optional<Recipe> findByIdAndUserId(Long id, Long userId);

    List<Recipe> findByUserIdAndNameContainingIgnoreCase(
            Long userId,
            String name
    );

//    List<Recipe> findAllByOrderByCreatedAtDesc();
//    List<Recipe> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
