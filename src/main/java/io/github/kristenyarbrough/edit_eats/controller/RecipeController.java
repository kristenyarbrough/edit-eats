package io.github.kristenyarbrough.edit_eats.controller;

import io.github.kristenyarbrough.edit_eats.domain.Recipe;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateRecipeRequest;
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
//    private final RecipeRepository repository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Recipe createRecipe(@Valid @RequestBody CreateRecipeRequest request) {
        return recipeService.createRecipe(request);
    }

//    @GetMapping
//    public Page<Recipe> getAll(@RequestParam(defaultValue = "0") int page,
//                               @RequestParam(defaultValue = "20") int size) {
//        return repository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
//    }
//
//    @GetMapping("/{id}")
//    public Recipe getById(@PathVariable Long id) {
//        return recipeService.getById(id);
//    }
//
//    @DeleteMapping("/{id}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void delete(@PathVariable Long id) {
//        recipeService.delete(id);
//    }
//
//    @PutMapping("/{id}")
//    public Recipe update(@PathVariable Long id,
//                         @Valid @RequestBody CreateRecipeRequest request) {
//        return recipeService.update(id, request);
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
