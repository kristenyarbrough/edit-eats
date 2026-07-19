package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.*;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateRecipeCategoryRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateRecipeIngredientRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateRecipeRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateRecipeStepRequest;
import io.github.kristenyarbrough.edit_eats.dto.response.RecipeCategoryResponse;
import io.github.kristenyarbrough.edit_eats.dto.response.RecipeIngredientResponse;
import io.github.kristenyarbrough.edit_eats.dto.response.RecipeResponse;
import io.github.kristenyarbrough.edit_eats.dto.response.RecipeStepResponse;
import io.github.kristenyarbrough.edit_eats.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeCategoryAssignmentRepository recipeCategoryAssignmentRepository;
    private final RecipeCategoryRepository recipeCategoryRepository;

    @Transactional
    public Recipe createRecipe(CreateRecipeRequest request) {

        if (request.getIngredients() == null || request.getIngredients().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Recipe must contain at least one ingredient");
        }

        if (request.getSteps() == null || request.getSteps().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Recipe must contain at least one step");
        }

        LocalDateTime now = LocalDateTime.now();

        Recipe recipe = Recipe.builder()
                .name(request.getName())
                .prepMinutes(request.getPrepMinutes())
                .cookMinutes(request.getCookMinutes())
                .difficulty(request.getDifficulty())
                .sourceUrl(request.getSourceUrl())
                .imageUrl(request.getImageUrl())
                .servings(request.getServings())
                .storageInstructions(request.getStorageInstructions())
                .freezerInstructions(request.getFreezerInstructions())
                .createdAt(now)
                .lastModifiedAt(now)
                .build();

        // Validate ingredients before saving recipe
        List<RecipeIngredient> recipeIngredients = new ArrayList<>();

        for (CreateRecipeIngredientRequest ingredientRequest : request.getIngredients()) {

            Ingredient ingredient = ingredientRepository.findById(
                    ingredientRequest.getIngredientId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Ingredient not found: " + ingredientRequest.getIngredientId()));

            recipeIngredients.add(
                    RecipeIngredient.builder()
                            .recipe(recipe)
                            .ingredient(ingredient)
                            .quantity(ingredientRequest.getQuantity())
                            .unit(ingredientRequest.getUnit())
                            .preparation(ingredientRequest.getPreparation())
                            .optional(ingredientRequest.getOptional())
                            .build()
            );

        }

        // Add steps to the recipe
        List<RecipeStep> steps = new ArrayList<>();

        for (int i = 0; i < request.getSteps().size(); i++) {

            CreateRecipeStepRequest stepRequest = request.getSteps().get(i);

            steps.add(
                    RecipeStep.builder()
                            .recipe(recipe)
                            .stepNumber(i + 1)
                            .instruction(stepRequest.getInstruction())
                            .build()
            );

        }

        // Validate categories before saving recipe
        List<RecipeCategoryAssignment> categoryAssignments = new ArrayList<>();

        if (request.getCategories() != null) {

            for (CreateRecipeCategoryRequest recipeCategoryRequest : request.getCategories()) {

                RecipeCategory recipeCategory =
                        recipeCategoryRepository.findById(recipeCategoryRequest.getRecipeCategoryId())
                                .orElseThrow(() -> new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Recipe category not found: " + recipeCategoryRequest.getRecipeCategoryId()));

                categoryAssignments.add(
                        RecipeCategoryAssignment.builder()
                                .recipe(recipe)
                                .recipeCategory(recipeCategory)
                                .build()
                );

            }

        }

        // Save after all validation has passed
        recipe = recipeRepository.save(recipe);
        recipeIngredientRepository.saveAll(recipeIngredients);
        recipeStepRepository.saveAll(steps);

        if (!categoryAssignments.isEmpty()) {

            recipeCategoryAssignmentRepository.saveAll(categoryAssignments);

        }

        return recipe;

    }

    public RecipeResponse getRecipe(Long recipeId) {

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Recipe not found: " + recipeId));

        List<RecipeIngredient> recipeIngredients = recipeIngredientRepository.findByRecipeId(recipeId);

        List<RecipeStep> recipeSteps = recipeStepRepository.findByRecipeIdOrderByStepNumber(recipeId);

        List<RecipeCategoryAssignment> categoryAssignments = recipeCategoryAssignmentRepository.findByRecipeId(recipeId);

        List<RecipeIngredientResponse> ingredientResponses =
                recipeIngredients.stream()
                        .map(recipeIngredient -> RecipeIngredientResponse.builder()
                                .ingredientId(recipeIngredient.getIngredient().getId())
                                .ingredientName(recipeIngredient.getIngredient().getName())
                                .quantity(recipeIngredient.getQuantity())
                                .unit(recipeIngredient.getUnit())
                                .preparation(recipeIngredient.getPreparation())
                                .optional(recipeIngredient.getOptional())
                                .build())
                        .toList();

        List<RecipeStepResponse> stepResponses =
                recipeSteps.stream()
                        .map(step -> RecipeStepResponse.builder()
                                .stepNumber(step.getStepNumber())
                                .instruction(step.getInstruction())
                                .build())
                        .toList();

        List<RecipeCategoryResponse> categoryResponses =
                categoryAssignments.stream()
                        .map(assignment -> RecipeCategoryResponse.builder()
                                .id(assignment.getRecipeCategory().getId())
                                .name(assignment.getRecipeCategory().getName())
                                .build())
                        .toList();

        return RecipeResponse.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .prepMinutes(recipe.getPrepMinutes())
                .cookMinutes(recipe.getCookMinutes())
                .totalMinutes(recipe.getTotalMinutes())
                .formattedTotalMinutes(recipe.getFormattedTotalTime())
                .servings(recipe.getServings())
                .difficulty(recipe.getDifficulty())
                .sourceUrl(recipe.getSourceUrl())
                .imageUrl(recipe.getImageUrl())
                .storageInstructions(recipe.getStorageInstructions())
                .freezerInstructions(recipe.getFreezerInstructions())
                .createdAt(recipe.getCreatedAt())
                .lastModifiedAt(recipe.getLastModifiedAt())
                .ingredients(ingredientResponses)
                .steps(stepResponses)
                .categories(categoryResponses)
                .build();
    }
}
//
//    @Transactional(readOnly = true)
//    public List<Recipe> getAll() {
//        return recipeRepository.findAllByOrderByCreatedAtDesc();
//    }
//
//    @Transactional(readOnly = true)
//    public Recipe getById(Long id) {
//        return recipeRepository.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Recipe not found"));
//    }
//
//    @Transactional
//    public void delete(Long id) {
//        if (!recipeRepository.existsById(id)) {
//            throw new ResponseStatusException(NOT_FOUND, "Recipe not found");
//        }
//        recipeRepository.deleteById(id);
//    }
//
//    @Transactional
//    public Recipe update(Long id, CreateRecipeRequest req) {
//        Recipe recipe = recipeRepository.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Recipe not found"));
//
//        recipe.setName(req.getName());
//        recipe.setMethod(req.getMethod());
//        recipe.setSourceUrl(req.getSourceUrl());
//        recipe.setPhotoUrl(req.getPhotoUrl());
//        recipe.setStorageInstructions(req.getStorageInstructions());
//        recipe.setFreezerInstructions(req.getFreezerInstructions());
//        recipe.setServings(req.getServings());
//        recipe.setPrepMinutes(req.getPrepMinutes());
//        recipe.setCookMinutes(req.getCookMinutes());
//
//        // clear old ingredients
//        recipe.getIngredients().clear();
//
//        for (var ing : req.getRecipeIngredientRequests()) {
//            recipe.getIngredients().add(
//                    RecipeIngredient.builder()
//                            .name(ing.getName())
//                            .quantity(ing.getQuantity())
//                            .unit(ing.getUnit())
//                            .notes(ing.getPreparation())
//                            .recipe(recipe)
//                            .build()
//            );
//        }

//        return recipeRepository.save(recipe);
//    }

//    @Transactional(readOnly = true)
//    public List<ShoppingListItem> generateShoppingList(ShoppingListRequest req) {
//
//        Map<String, ShoppingListItem> combined = new HashMap<>();
//
//        for (Long id : req.getRecipeIds()) {
//            Recipe recipe = getById(id);
//
//            for (var ing : recipe.getIngredients()) {
//                String key = ing.getName().trim().toLowerCase();
//
//                if (!combined.containsKey(key)) {
//                    combined.put(key,
//                            new ShoppingListItem(
//                                    ing.getName(),
//                                    ing.getQuantity(),
//                                    ing.getUnit()
//                            ));
//                } else {
//                    ShoppingListItem existing = combined.get(key);
//
//                    BigDecimal convertedQty =
//                            UnitConverter.convert(
//                                    ing.getQuantity(),
//                                    ing.getUnit(),
//                                    existing.getUnit()
//                            );
//
//                    existing.setQuantity(existing.getQuantity().add(convertedQty));
//                }
//            }
//        }
//
//        return combined.values().stream()
//                .sorted(Comparator.comparing(i -> i.getIngredient().toLowerCase()))
//                .map(i -> new ShoppingListItem(
//                        i.getIngredient(),
//                        i.getQuantity() == null ? null : i.getQuantity().setScale(2, RoundingMode.HALF_UP),
//                        i.getUnit()
//                ))
//                .toList();
//    }
//
//    @Transactional(readOnly = true)
//    public List<ShoppingListItem> generateShoppingListByRecipeIds(List<Long> recipeIds) {
//        ShoppingListRequest req = new ShoppingListRequest();
//        req.setRecipeIds(recipeIds);
//        return generateShoppingList(req);
//    }

