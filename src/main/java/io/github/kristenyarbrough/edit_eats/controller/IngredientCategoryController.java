package io.github.kristenyarbrough.edit_eats.controller;

import io.github.kristenyarbrough.edit_eats.domain.IngredientCategory;
import io.github.kristenyarbrough.edit_eats.dto.CreateIngredientCategoryRequest;
import io.github.kristenyarbrough.edit_eats.service.IngredientCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingredient-categories")
@RequiredArgsConstructor
public class IngredientCategoryController {

    private final IngredientCategoryService ingredientCategoryService;

    @PostMapping
    public IngredientCategory createIngredientCategory(
            @Valid @RequestBody CreateIngredientCategoryRequest request) {

        return ingredientCategoryService.createIngredientCategory(request);

    }
}
