package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.Ingredient;
import io.github.kristenyarbrough.edit_eats.domain.IngredientCategory;
import io.github.kristenyarbrough.edit_eats.dto.CreateIngredientRequest;
import io.github.kristenyarbrough.edit_eats.repository.IngredientCategoryRepository;
import io.github.kristenyarbrough.edit_eats.repository.IngredientRepository;
import org.springframework.stereotype.Service;

@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientCategoryRepository ingredientCategoryRepository;

    public IngredientService(IngredientRepository ingredientRepository,
                             IngredientCategoryRepository ingredientCategoryRepository) {
        this.ingredientRepository = ingredientRepository;
        this.ingredientCategoryRepository = ingredientCategoryRepository;
    }

    public Ingredient createIngredient(CreateIngredientRequest request) {

        ingredientRepository.findByName(request.getName())
                .ifPresent(ingredient -> {
                    throw new IllegalArgumentException(
                            "An ingredient with the name '" + request.getName() + "' already exists.");
        });

        IngredientCategory category =
                ingredientCategoryRepository
                        .findById(request.getIngredientCategoryId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Ingredient category not found."));

        Ingredient ingredient = Ingredient.builder()
                .name(request.getName())
                .defaultUnit(request.getDefaultUnit())
                .ingredientCategory(category)
                .build();

        return ingredientRepository.save(ingredient);

    }
}
