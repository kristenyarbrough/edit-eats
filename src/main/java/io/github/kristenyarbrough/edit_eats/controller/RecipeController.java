package io.github.kristenyarbrough.edit_eats.controller;

import io.github.kristenyarbrough.edit_eats.domain.Recipe;
import io.github.kristenyarbrough.edit_eats.dto.imported.ImportedRecipe;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateRecipeRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.UpdateRecipeRequest;
import io.github.kristenyarbrough.edit_eats.dto.response.RecipeDraftResponse;
import io.github.kristenyarbrough.edit_eats.dto.response.RecipeResponse;
import io.github.kristenyarbrough.edit_eats.service.RecipeImportService;
import io.github.kristenyarbrough.edit_eats.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final RecipeImportService recipeImportService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Recipe createRecipe(@Valid @RequestBody CreateRecipeRequest request) {
        return recipeService.createRecipe(request);
    }

    @GetMapping("/{id}")
    public RecipeResponse getRecipe(@PathVariable Long id) {
        return recipeService.getRecipe(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
    }

    @PutMapping("/{id}")
    public Recipe updateRecipe(@PathVariable Long id,
                         @Valid @RequestBody UpdateRecipeRequest request) {
        return recipeService.updateRecipe(id, request);
    }

    @PostMapping("/import/url")
    public RecipeDraftResponse importRecipeFromUrl(
            @RequestParam String url) {

        return recipeImportService.importRecipeDraftFromUrl(url);

    }

    @PostMapping("/import/text")
    public RecipeDraftResponse importRecipeFromText(
            @RequestParam String text) {

        return recipeImportService.importRecipeDraftFromText(text);

    }

//    @GetMapping
//    public Page<Recipe> getAll(@RequestParam(defaultValue = "0") int page,
//                               @RequestParam(defaultValue = "20") int size) {
//        return repository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
//    }
//
//    @PostMapping("/shopping-list")
//    public List<ShoppingListItem> shoppingList(@RequestBody ShoppingListRequest request) {
//        return recipeService.generateShoppingList(request);
//    }
//
//    @GetMapping("/search")
//    public List<Recipe> search(@RequestParam String name,
//                               @RequestParam(defaultValue = "0") int page,
//                               @RequestParam(defaultValue = "20") int size) {
//        return repository.findByNameContainingIgnoreCase(
//                name,
//                PageRequest.of(page, size, Sort.by("createdAt").descending())
//        );
//    }
}
