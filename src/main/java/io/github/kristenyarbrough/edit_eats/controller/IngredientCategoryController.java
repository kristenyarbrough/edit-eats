    package io.github.kristenyarbrough.edit_eats.controller;

    import io.github.kristenyarbrough.edit_eats.domain.IngredientCategory;
    import io.github.kristenyarbrough.edit_eats.dto.request.CreateIngredientCategoryRequest;
    import io.github.kristenyarbrough.edit_eats.service.IngredientCategoryService;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.HttpStatus;
    import org.springframework.web.bind.annotation.*;

    @RestController
    @RequestMapping("/api/ingredient-categories")
    @RequiredArgsConstructor
    public class IngredientCategoryController {

        private final IngredientCategoryService ingredientCategoryService;

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public IngredientCategory createIngredientCategory(
                @Valid @RequestBody CreateIngredientCategoryRequest request) {

            return ingredientCategoryService.createIngredientCategory(request);

        }
    }
