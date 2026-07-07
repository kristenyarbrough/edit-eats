package io.github.kristenyarbrough.edit_eats.controller;

import io.github.kristenyarbrough.edit_eats.domain.Ingredient;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateIngredientRequest;
import io.github.kristenyarbrough.edit_eats.service.IngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Ingredient createIngredient(
            @Valid @RequestBody CreateIngredientRequest request) {

        return ingredientService.createIngredient(request);

    }

    @GetMapping
    public List<Ingredient> getIngredients(
            @RequestParam(required = false) String name) {

        if (name == null || name.isBlank()) {
            return ingredientService.getAllIngredients();
        }

        return ingredientService.findIngredients(name);
    }
}
