package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.Recipe;
import io.github.kristenyarbrough.edit_eats.domain.RecipeStep;
import io.github.kristenyarbrough.edit_eats.dto.CreateRecipeRequest;
import io.github.kristenyarbrough.edit_eats.repository.RecipeRepository;
import io.github.kristenyarbrough.edit_eats.repository.RecipeStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeStepRepository recipeStepRepository;

    @Transactional
    public Recipe createRecipe(CreateRecipeRequest request) {
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
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        recipe = recipeRepository.save(recipe);

        List<RecipeStep> steps = new ArrayList<>();

        int stepNumber = 1;

        for (CreateRecipeRequest.Step stepRequest : request.getSteps()) {
            steps.add(
                    RecipeStep.builder()
                            .recipe(recipe)
                            .stepNumber(stepNumber++)
                            .instruction(stepRequest.getInstruction())
                            .build()
            );
        }

        recipeStepRepository.saveAll(steps);

//        for (var ing : request.getRecipeIngredientRequests()) {
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
//
        return recipeRepository.save(recipe);
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
}
