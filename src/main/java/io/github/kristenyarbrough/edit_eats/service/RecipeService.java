package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.*;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateRecipeCategoryRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateRecipeIngredientRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateRecipeRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateRecipeStepRequest;
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

        recipe = recipeRepository.save(recipe);

        List<RecipeIngredient> recipeIngredients = new ArrayList<>();

        for (CreateRecipeIngredientRequest ingredientRequest : request.getIngredients()) {
            Ingredient ingredient = ingredientRepository.findById(ingredientRequest.getIngredientId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Ingredient not found: " + ingredientRequest.getIngredientId()));
            RecipeIngredient recipeIngredient = RecipeIngredient.builder()
                    .recipe(recipe)
                    .ingredient(ingredient)
                    .quantity(ingredientRequest.getQuantity())
                    .unit(ingredientRequest.getUnit())
                    .preparation(ingredientRequest.getPreparation())
                    .optional(ingredientRequest.getOptional())
                    .build();

            recipeIngredients.add(recipeIngredient);
        }

        recipeIngredientRepository.saveAll(recipeIngredients);

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

        recipeStepRepository.saveAll(steps);

        List<RecipeCategoryAssignment> categoryAssignments = new ArrayList<>();

        for (CreateRecipeCategoryRequest recipeCategoryRequest : request.getCategories()) {

            RecipeCategory recipeCategory =
                    recipeCategoryRepository.findById(recipeCategoryRequest.getRecipeCategoryId())
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Recipe category not found: " + recipeCategoryRequest.getRecipeCategoryId()));

            RecipeCategoryAssignment recipeCategoryAssignment = RecipeCategoryAssignment.builder()
                    .recipe(recipe)
                    .recipeCategory(recipeCategory)
                    .build();

            categoryAssignments.add(recipeCategoryAssignment);
        }

        recipeCategoryAssignmentRepository.saveAll(categoryAssignments);

        return recipe;
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

