package io.github.kristenyarbrough.edit_eats.controller;

import io.github.kristenyarbrough.edit_eats.service.RecipeImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recipe-import")
@RequiredArgsConstructor
public class RecipeImportController {

    private final RecipeImportService recipeImportService;

    // URL endpoint

    // Text endpoint

}
