package io.github.kristenyarbrough.edit_eats.controller;

import io.github.kristenyarbrough.edit_eats.domain.Recipe;
import io.github.kristenyarbrough.edit_eats.dto.CreateRecipeRequest;
import io.github.kristenyarbrough.edit_eats.dto.ShoppingListItem;
import io.github.kristenyarbrough.edit_eats.dto.ShoppingListRequest;
import io.github.kristenyarbrough.edit_eats.repo.RecipeRepository;
import io.github.kristenyarbrough.edit_eats.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final RecipeRepository repository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Recipe create(@Valid @RequestBody CreateRecipeRequest request) {
        return recipeService.create(request);
    }

    @GetMapping
    public Page<Recipe> getAll(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size) {
        return repository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @GetMapping("/{id}")
    public Recipe getById(@PathVariable Long id) {
        return recipeService.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        recipeService.delete(id);
    }

    @PutMapping("/{id}")
    public Recipe update(@PathVariable Long id,
                         @Valid @RequestBody CreateRecipeRequest request) {
        return recipeService.update(id, request);
    }

    @PostMapping("/shopping-list")
    public List<ShoppingListItem> shoppingList(@RequestBody ShoppingListRequest request) {
        return recipeService.generateShoppingList(request);
    }

    @GetMapping("/search")
    public List<Recipe> search(@RequestParam String name,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size) {
        return repository.findByNameContainingIgnoreCase(
                name,
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
    }
}
