package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.MealPlanRecipe;
import io.github.kristenyarbrough.edit_eats.domain.RecipeIngredient;
import io.github.kristenyarbrough.edit_eats.dto.response.ShoppingListCategoryResponse;
import io.github.kristenyarbrough.edit_eats.dto.response.ShoppingListItemResponse;
import io.github.kristenyarbrough.edit_eats.dto.response.ShoppingListResponse;
import io.github.kristenyarbrough.edit_eats.repository.MealPlanRecipeRepository;
import io.github.kristenyarbrough.edit_eats.repository.MealPlanRepository;
import io.github.kristenyarbrough.edit_eats.repository.RecipeIngredientRepository;
import io.github.kristenyarbrough.edit_eats.util.ConvertedQuantity;
import io.github.kristenyarbrough.edit_eats.util.UnitConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final MealPlanRecipeRepository mealPlanRecipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final MealPlanRepository mealPlanRepository;

    @Transactional(readOnly = true)
    public ShoppingListResponse generateShoppingList(Long mealPlanId) {

        mealPlanRepository.findById(mealPlanId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Meal plan not found: " + mealPlanId));

        List<MealPlanRecipe> meals =
                mealPlanRecipeRepository.findByMealPlanId(mealPlanId);

        Map<Long, ShoppingListItemResponse> shoppingList =
                new LinkedHashMap<>();

        for (MealPlanRecipe meal : meals) {

            int recipeServings = meal.getRecipe().getServings();
            int mealServings = meal.getServings();

            BigDecimal scalingFactor = BigDecimal.valueOf(mealServings)
                    .divide(
                            BigDecimal.valueOf(recipeServings),
                            10,
                            RoundingMode.HALF_UP
                    );

            List<RecipeIngredient> recipeIngredients =
                    recipeIngredientRepository.findByRecipeId(
                            meal.getRecipe().getId());

            for (RecipeIngredient recipeIngredient : recipeIngredients) {

                BigDecimal scaledQuantity =
                        recipeIngredient.getQuantity()
                                .multiply(scalingFactor);

                Long key = recipeIngredient.getIngredient().getId();

                ShoppingListItemResponse existing = shoppingList.get(key);

                if (existing == null) {

                    shoppingList.put(
                            key,
                            ShoppingListItemResponse.builder()
                                    .ingredientId(
                                            recipeIngredient.getIngredient().getId())
                                    .ingredientName(
                                            recipeIngredient.getIngredient().getName())
                                    .ingredientCategoryId(
                                            recipeIngredient.getIngredient()
                                                    .getIngredientCategory()
                                                    .getId())
                                    .ingredientCategoryName(
                                            recipeIngredient.getIngredient()
                                                    .getIngredientCategory()
                                                    .getName())
                                    .quantity(scaledQuantity)
                                    .unit(recipeIngredient.getUnit())
                                    .build()
                    );

                } else {

                    BigDecimal convertedQuantity;
                    try {

                        convertedQuantity =
                                UnitConverter.convert(
                                        scaledQuantity,
                                        recipeIngredient.getUnit(),
                                        existing.getUnit()
                                );

                    } catch (IllegalArgumentException e) {

                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Cannot combine units "
                                    + existing.getUnit()
                                    + " and "
                                    + recipeIngredient.getUnit()
                                    + " for ingredient: "
                                    + recipeIngredient.getIngredient().getName(),
                                e
                        );

                    }

                    existing.setQuantity(
                            existing.getQuantity()
                                    .add(convertedQuantity)
                    );

                }

            }
        }

        List<ShoppingListItemResponse> result = new ArrayList<>(shoppingList.values());

        for (ShoppingListItemResponse item : result) {

            ConvertedQuantity normalised =
                    UnitConverter.normalise(
                            item.getQuantity(),
                            item.getUnit());

            item.setQuantity(normalised.quantity());
            item.setUnit(normalised.unit());

        }

        Map<Long, ShoppingListCategoryResponse> categories = new LinkedHashMap<>();

        for (ShoppingListItemResponse item : result) {

            ShoppingListCategoryResponse category =
                    categories.computeIfAbsent(
                            item.getIngredientCategoryId(),
                            id -> ShoppingListCategoryResponse.builder()
                                    .categoryId(id)
                                    .categoryName(item.getIngredientCategoryName())
                                    .items(new ArrayList<>())
                                    .build()
                    );

            category.getItems().add(item);

        }

        return ShoppingListResponse.builder()
                .categories(new ArrayList<>(categories.values()))
                .build();

    }

}
