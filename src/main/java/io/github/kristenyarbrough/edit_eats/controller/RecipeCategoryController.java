package io.github.kristenyarbrough.edit_eats.controller;

import io.github.kristenyarbrough.edit_eats.domain.RecipeCategory;
import io.github.kristenyarbrough.edit_eats.dto.response.RecipeCategoryResponse;
import io.github.kristenyarbrough.edit_eats.service.RecipeCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipe-categories")
@RequiredArgsConstructor
public class RecipeCategoryController {

    private final RecipeCategoryService recipeCategoryService;

    @GetMapping
    public List<RecipeCategoryResponse> getAvailableCategories() {

        return recipeCategoryService.getAvailableCategories();

    }

    @PostMapping
    public RecipeCategoryResponse createCategory(@RequestParam String name) {

        return recipeCategoryService.createCategory(name);

    }

}
