package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.MealPlan;
import io.github.kristenyarbrough.edit_eats.domain.MealPlanRecipe;
import io.github.kristenyarbrough.edit_eats.domain.Recipe;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateMealPlanRecipeRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateMealPlanRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.UpdateMealPlanRecipeRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.UpdateMealPlanRequest;
import io.github.kristenyarbrough.edit_eats.dto.response.MealPlanRecipeResponse;
import io.github.kristenyarbrough.edit_eats.dto.response.MealPlanResponse;
import io.github.kristenyarbrough.edit_eats.repository.MealPlanRecipeRepository;
import io.github.kristenyarbrough.edit_eats.repository.MealPlanRepository;
import io.github.kristenyarbrough.edit_eats.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final MealPlanRecipeRepository mealPlanRecipeRepository;
    private final RecipeRepository recipeRepository;

    @Transactional
    public MealPlanRecipe addRecipeToMealPlan(
            Long mealPlanId, CreateMealPlanRecipeRequest request) {

        MealPlan mealPlan = mealPlanRepository.findById(mealPlanId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Meal plan not found: " + mealPlanId));

        Recipe recipe = recipeRepository.findById(request.getRecipeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Recipe not found: " + request.getRecipeId()));

        MealPlanRecipe mealPlanRecipe = MealPlanRecipe.builder()
                .mealPlan(mealPlan)
                .recipe(recipe)
                .mealDate(request.getMealDate())
                .mealType(request.getMealType())
                .servings(request.getServings())
                .build();

        return mealPlanRecipeRepository.save(mealPlanRecipe);

    }

    @Transactional
    public MealPlanRecipe updateMealPlanRecipe(Long mealPlanRecipeId, UpdateMealPlanRecipeRequest request) {

        MealPlanRecipe mealPlanRecipe = mealPlanRecipeRepository.findById(mealPlanRecipeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Meal plan recipe not found: " + mealPlanRecipeId));

        Recipe recipe = recipeRepository.findById(request.getRecipeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Recipe not found: " + request.getRecipeId()));

        mealPlanRecipe.setRecipe(recipe);
        mealPlanRecipe.setMealDate(request.getMealDate());
        mealPlanRecipe.setMealType(request.getMealType());
        mealPlanRecipe.setServings(request.getServings());

        mealPlanRecipe.getMealPlan()
                .setLastModifiedAt(LocalDateTime.now());

        return mealPlanRecipeRepository.save(mealPlanRecipe);

    }

    @Transactional
    public void deleteMealPlanRecipe(Long mealPlanRecipeId) {

        MealPlanRecipe mealPlanRecipe = mealPlanRecipeRepository.findById(mealPlanRecipeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Meal plan recipe not found: " + mealPlanRecipeId));

        MealPlan mealPlan = mealPlanRecipe.getMealPlan();

        mealPlanRecipeRepository.delete(mealPlanRecipe);

        mealPlan.setLastModifiedAt(LocalDateTime.now());

    }

    public MealPlan createMealPlan(CreateMealPlanRequest request) {

        MealPlan mealPlan = MealPlan.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        return mealPlanRepository.save(mealPlan);

    }

    public List<MealPlan> getMealPlans() {

        return mealPlanRepository.findAll();
    }

    public MealPlanResponse getMealPlan(Long id) {

        MealPlan mealPlan = mealPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Meal plan not found: " + id));

        List<MealPlanRecipeResponse> meals = mealPlanRecipeRepository
                .findByMealPlanIdOrderByMealDateAscMealTypeAsc(id)
                .stream()
                .map(meal -> MealPlanRecipeResponse.builder()
                        .id(meal.getId())
                        .recipeId(meal.getRecipe().getId())
                        .recipeName(meal.getRecipe().getName())
                        .mealDate(meal.getMealDate())
                        .mealType(meal.getMealType())
                        .servings(meal.getServings())
                        .build())
                .toList();

        return MealPlanResponse.builder()
                .id(mealPlan.getId())
                .name(mealPlan.getName())
                .startDate(mealPlan.getStartDate())
                .createdAt(mealPlan.getCreatedAt())
                .lastModifiedAt(mealPlan.getLastModifiedAt())
                .meals(meals)
                .build();

    }

    public List<MealPlan> findMealPlans(String name) {

        return mealPlanRepository.findByNameContainingIgnoreCase(name);

    }

    @Transactional
    public MealPlan updateMealPlan(Long id, UpdateMealPlanRequest request) {

        MealPlan mealPlan = mealPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Meal plan not found: " + id));

        mealPlan.setName(request.getName());
        mealPlan.setStartDate(request.getStartDate());
        mealPlan.setLastModifiedAt(LocalDateTime.now());

        return mealPlanRepository.save(mealPlan);

    }

    @Transactional
    public void deleteMealPlan(Long id) {

        MealPlan mealPlan = mealPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Meal plan not found: " + id));

        mealPlanRecipeRepository.deleteByMealPlanId(id);
        mealPlanRepository.delete(mealPlan);

    }
}
