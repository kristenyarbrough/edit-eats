package io.github.kristenyarbrough.edit_eats.repository;

import io.github.kristenyarbrough.edit_eats.domain.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Optional<Ingredient> findByUserIdAndNameIgnoreCase(Long userId, String name);

    Optional<Ingredient> findByIdAndUserId(Long id, Long userId);

    List<Ingredient> findTop20ByUserIdAndNameContainingIgnoreCase(Long userId, String name);

    List<Ingredient> findAllByUserIdOrderByNameAsc(Long userId);

}
