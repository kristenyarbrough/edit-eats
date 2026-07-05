package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.IngredientCategory;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateIngredientCategoryRequest;
import io.github.kristenyarbrough.edit_eats.repository.IngredientCategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class IngredientCategoryService {

    private final IngredientCategoryRepository ingredientCategoryRepository;

    public IngredientCategoryService(IngredientCategoryRepository ingredientCategoryRepository) {
        this.ingredientCategoryRepository = ingredientCategoryRepository;
    }

    public IngredientCategory createIngredientCategory(CreateIngredientCategoryRequest request) {

        ingredientCategoryRepository.findByName(request.getName())
                .ifPresent(category -> {
                    throw new IllegalArgumentException(
                            "Category '" + request.getName() + "'already exists.");
                });

        IngredientCategory category = IngredientCategory.builder()
                .name(request.getName())
                .build();

        return ingredientCategoryRepository.save(category);

    }
}
