package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.*;
import io.github.kristenyarbrough.edit_eats.dto.response.ShoppingListItemResponse;
import io.github.kristenyarbrough.edit_eats.repository.MealPlanRecipeRepository;
import io.github.kristenyarbrough.edit_eats.repository.MealPlanRepository;
import io.github.kristenyarbrough.edit_eats.repository.RecipeIngredientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    @Mock
    private MealPlanRecipeRepository mealPlanRecipeRepository;

    @Mock
    private RecipeIngredientRepository recipeIngredientRepository;

    @Mock
    private MealPlanRepository mealPlanRepository;

    @InjectMocks
    private ShoppingListService shoppingListService;

    @BeforeEach
    void setUp() {

        MealPlan mealPlan  = MealPlan.builder()
                .id(1L)
                .name("Family Meals")
                .build();

        lenient().when(mealPlanRepository.findById(1L))
                .thenReturn(Optional.of(mealPlan));

    }

    @Test
    void shouldGenerateShoppingListForMealPlan() {

        Recipe recipe = createRecipe();

        MealPlan mealPlan = createMealPlan();

        MealPlanRecipe mealPlanRecipe = createMealPlanRecipe(mealPlan, recipe);

        Ingredient ingredient = createIngredient();

        RecipeIngredient recipeIngredient = createRecipeIngredient(recipe, ingredient);

        when(mealPlanRecipeRepository.findByMealPlanId(1L))
                .thenReturn(List.of(mealPlanRecipe));

        when(recipeIngredientRepository.findByRecipeId(1L))
                .thenReturn(List.of(recipeIngredient));

        List<ShoppingListItemResponse> result =
                shoppingListService.generateShoppingList(1L);

        assertEquals(1, result.size());

        ShoppingListItemResponse item = result.get(0);

        assertEquals(1L, item.getIngredientId());
        assertEquals("Flour", item.getIngredientName());
        assertEquals(0, new BigDecimal("500").compareTo(item.getQuantity()));
        assertEquals(Unit.G, item.getUnit());
        assertEquals(1L, item.getIngredientCategoryId());
        assertEquals("Pantry", item.getIngredientCategoryName());

        verify(mealPlanRecipeRepository).findByMealPlanId(1L);
        verify(recipeIngredientRepository).findByRecipeId(1L);

    }

    @Test
    void shouldScaleIngredientsWhenMealServingsDifferFromRecipeServings() {

        Recipe recipe = createRecipe();

        MealPlan mealPlan = createMealPlan();

        MealPlanRecipe mealPlanRecipe = createMealPlanRecipe(mealPlan, recipe);
        mealPlanRecipe.setServings(6);

        Ingredient ingredient = createIngredient();

        RecipeIngredient recipeIngredient = createRecipeIngredient(recipe, ingredient);

        when(mealPlanRecipeRepository.findByMealPlanId(1L))
                .thenReturn(List.of(mealPlanRecipe));

        when(recipeIngredientRepository.findByRecipeId(1L))
                .thenReturn(List.of(recipeIngredient));

        List<ShoppingListItemResponse> result =
                shoppingListService.generateShoppingList(1L);

        assertEquals(1, result.size());

        ShoppingListItemResponse item = result.get(0);

        assertEquals(1L, item.getIngredientId());
        assertEquals("Flour", item.getIngredientName());
        assertEquals(0, new BigDecimal("750").compareTo(item.getQuantity()));
        assertEquals(Unit.G, item.getUnit());
        assertEquals(1L, item.getIngredientCategoryId());
        assertEquals("Pantry", item.getIngredientCategoryName());

        verify(mealPlanRecipeRepository).findByMealPlanId(1L);
        verify(recipeIngredientRepository).findByRecipeId(1L);

    }

    @Test
    void shouldCombineSameIngredientAcrossMeals() {

        Recipe recipe1 = createRecipe();

        Recipe recipe2 = Recipe.builder()
                .id(2L)
                .name("Bread")
                .servings(4)
                .build();

        MealPlan mealPlan = createMealPlan();

        MealPlanRecipe meal1 = createMealPlanRecipe(mealPlan, recipe1);

        MealPlanRecipe meal2 = MealPlanRecipe.builder()
                .id(2L)
                .mealPlan(mealPlan)
                .recipe(recipe2)
                .mealDate(LocalDate.of(2026, 8, 19))
                .mealType(MealType.DINNER)
                .servings(4)
                .build();

        Ingredient flour = createIngredient();

        RecipeIngredient flourForPancakes = createRecipeIngredient(recipe1, flour);

        RecipeIngredient flourForBread = RecipeIngredient.builder()
                .id(2L)
                .recipe(recipe2)
                .ingredient(flour)
                .quantity(new BigDecimal("300"))
                .unit(Unit.G)
                .build();

        when(mealPlanRecipeRepository.findByMealPlanId(1L))
                .thenReturn(List.of(meal1, meal2));

        when(recipeIngredientRepository.findByRecipeId(1L))
                .thenReturn(List.of(flourForPancakes));

        when(recipeIngredientRepository.findByRecipeId(2L))
                .thenReturn(List.of(flourForBread));

        List<ShoppingListItemResponse> result =
                shoppingListService.generateShoppingList(1L);

        assertEquals(1, result.size());

        ShoppingListItemResponse item = result.get(0);

        assertEquals(1L, item.getIngredientId());
        assertEquals("Flour", item.getIngredientName());
        assertEquals(0, new BigDecimal("800").compareTo(item.getQuantity()));
        assertEquals(Unit.G, item.getUnit());
        assertEquals(1L, item.getIngredientCategoryId());
        assertEquals("Pantry", item.getIngredientCategoryName());

        verify(mealPlanRecipeRepository).findByMealPlanId(1L);
        verify(recipeIngredientRepository).findByRecipeId(1L);
        verify(recipeIngredientRepository).findByRecipeId(2L);

    }

    @Test
    void shouldCombineSameIngredientWhenUnitsDiffer() {

        Recipe recipe1 = createRecipe();

        Recipe recipe2 = Recipe.builder()
                .id(2L)
                .name("Bread")
                .servings(4)
                .build();

        MealPlan mealPlan = createMealPlan();

        MealPlanRecipe meal1 = createMealPlanRecipe(mealPlan, recipe1);

        MealPlanRecipe meal2 = MealPlanRecipe.builder()
                .id(2L)
                .mealPlan(mealPlan)
                .recipe(recipe2)
                .mealDate(LocalDate.of(2026, 8, 19))
                .mealType(MealType.DINNER)
                .servings(4)
                .build();

        Ingredient flour = createIngredient();

        RecipeIngredient flourInPancakes = createRecipeIngredient(recipe1, flour);

        RecipeIngredient flourInBread = RecipeIngredient.builder()
                .id(2L)
                .recipe(recipe2)
                .ingredient(flour)
                .quantity(new BigDecimal(1))
                .unit(Unit.KG)
                .build();

        when(mealPlanRecipeRepository.findByMealPlanId(1L))
                .thenReturn(List.of(meal1, meal2));

        when(recipeIngredientRepository.findByRecipeId(1L))
                .thenReturn(List.of(flourInPancakes));

        when(recipeIngredientRepository.findByRecipeId(2L))
                .thenReturn(List.of(flourInBread));

        List<ShoppingListItemResponse> result =
                shoppingListService.generateShoppingList(1L);

        assertEquals(1, result.size());

        ShoppingListItemResponse item = result.get(0);

        assertEquals(1L, item.getIngredientId());
        assertEquals("Flour", item.getIngredientName());
        assertEquals(0, new BigDecimal("1.5").compareTo(item.getQuantity()));
        assertEquals(Unit.KG, item.getUnit());
        assertEquals(1L, item.getIngredientCategoryId());
        assertEquals("Pantry", item.getIngredientCategoryName());

    }

    @Test
    void shouldNormaliseCombinedMilliltresToLitres() {

        Recipe recipe1 = createRecipe();
        recipe1.setName("Tomato Soup");

        Recipe recipe2 = Recipe.builder()
                .id(2L)
                .name("Cream Sauce")
                .servings(4)
                .build();

        MealPlan mealPlan = createMealPlan();

        MealPlanRecipe meal1 = createMealPlanRecipe(mealPlan, recipe1);

        MealPlanRecipe meal2 = MealPlanRecipe.builder()
                .id(2L)
                .mealPlan(mealPlan)
                .recipe(recipe2)
                .mealDate(LocalDate.of(2026, 8, 19))
                .mealType(MealType.DINNER)
                .servings(4)
                .build();

        IngredientCategory category = IngredientCategory.builder()
                .id(1L)
                .name("Dairy")
                .build();

        Ingredient milk = Ingredient.builder()
                .id(1L)
                .name("Milk")
                .defaultUnit(Unit.ML)
                .ingredientCategory(category)
                .build();

        RecipeIngredient milkForSoup = RecipeIngredient.builder()
                .id(1L)
                .recipe(recipe1)
                .ingredient(milk)
                .quantity(new BigDecimal("800"))
                .unit(Unit.ML)
                .build();

        RecipeIngredient milkForSauce = RecipeIngredient.builder()
                .id(2L)
                .recipe(recipe2)
                .ingredient(milk)
                .quantity(new BigDecimal("800"))
                .unit(Unit.ML)
                .build();

        when(mealPlanRecipeRepository.findByMealPlanId(1L))
                .thenReturn(List.of(meal1, meal2));

        when(recipeIngredientRepository.findByRecipeId(1L))
                .thenReturn(List.of(milkForSoup));

        when(recipeIngredientRepository.findByRecipeId(2L))
                .thenReturn(List.of(milkForSauce));

        List<ShoppingListItemResponse> result =
                shoppingListService.generateShoppingList(1L);

        assertEquals(1, result.size());

        ShoppingListItemResponse item = result.get(0);

        assertEquals(1L, item.getIngredientId());
        assertEquals("Milk", item.getIngredientName());
        assertEquals(0,
                new BigDecimal("1.6").compareTo(item.getQuantity()));
        assertEquals(Unit.L, item.getUnit());
        assertEquals(1L, item.getIngredientCategoryId());
        assertEquals("Dairy", item.getIngredientCategoryName());

        verify(mealPlanRecipeRepository).findByMealPlanId(1L);
        verify(recipeIngredientRepository).findByRecipeId(1L);
        verify(recipeIngredientRepository).findByRecipeId(2L);

    }

    @Test
    void shouldCombineGramsAndKilogramsRegardlessOfOrder() {

        Recipe recipe1 = createRecipe();

        Recipe recipe2 = Recipe.builder()
                .id(2L)
                .name("Bread")
                .servings(4)
                .build();

        MealPlan mealPlan = createMealPlan();

        MealPlanRecipe meal1 = createMealPlanRecipe(mealPlan, recipe1);

        MealPlanRecipe meal2 = MealPlanRecipe.builder()
                .id(2L)
                .mealPlan(mealPlan)
                .recipe(recipe2)
                .mealDate(LocalDate.of(2026, 8, 19))
                .mealType(MealType.DINNER)
                .servings(4)
                .build();

        Ingredient flour = createIngredient();

        RecipeIngredient flourInPancakes = createRecipeIngredient(recipe1, flour);
        flourInPancakes.setQuantity(new BigDecimal(1));
        flourInPancakes.setUnit(Unit.KG);

        RecipeIngredient flourInBread = RecipeIngredient.builder()
                .id(2L)
                .recipe(recipe2)
                .ingredient(flour)
                .quantity(new BigDecimal(500))
                .unit(Unit.G)
                .build();

        when(mealPlanRecipeRepository.findByMealPlanId(1L))
                .thenReturn(List.of(meal1, meal2));

        when(recipeIngredientRepository.findByRecipeId(1L))
                .thenReturn(List.of(flourInPancakes));

        when(recipeIngredientRepository.findByRecipeId(2L))
                .thenReturn(List.of(flourInBread));

        List<ShoppingListItemResponse> result =
                shoppingListService.generateShoppingList(1L);

        assertEquals(1, result.size());

        ShoppingListItemResponse item = result.get(0);

        assertEquals(1L, item.getIngredientId());
        assertEquals("Flour", item.getIngredientName());
        assertEquals(0, new BigDecimal("1.5").compareTo(item.getQuantity()));
        assertEquals(Unit.KG, item.getUnit());
        assertEquals(1L, item.getIngredientCategoryId());
        assertEquals("Pantry", item.getIngredientCategoryName());

    }

    @Test
    void shouldCombineMultipleQuantitiesAcrossCompatibleUnits() {

        Recipe recipe1 = Recipe.builder()
                .id(1L)
                .name("Soup")
                .servings(4)
                .build();

        Recipe recipe2 = Recipe.builder()
                .id(2L)
                .name("Sauce")
                .servings(4)
                .build();

        Recipe recipe3 = Recipe.builder()
                .id(3L)
                .name("Curry")
                .servings(4)
                .build();

        MealPlan mealPlan = MealPlan.builder()
                .id(1L)
                .name("Family Meals")
                .build();

        MealPlanRecipe meal1 = MealPlanRecipe.builder()
                .id(1L)
                .mealPlan(mealPlan)
                .recipe(recipe1)
                .mealDate(LocalDate.of(2026, 8, 18))
                .mealType(MealType.DINNER)
                .servings(4)
                .build();

        MealPlanRecipe meal2 = MealPlanRecipe.builder()
                .id(2L)
                .mealPlan(mealPlan)
                .recipe(recipe2)
                .mealDate(LocalDate.of(2026, 8, 19))
                .mealType(MealType.DINNER)
                .servings(4)
                .build();

        MealPlanRecipe meal3 = MealPlanRecipe.builder()
                .id(3L)
                .mealPlan(mealPlan)
                .recipe(recipe3)
                .mealDate(LocalDate.of(2026, 8, 20))
                .mealType(MealType.DINNER)
                .servings(4)
                .build();

        IngredientCategory category = IngredientCategory.builder()
                .id(1L)
                .name("Dairy")
                .build();

        Ingredient milk = Ingredient.builder()
                .id(1L)
                .name("Milk")
                .defaultUnit(Unit.ML)
                .ingredientCategory(category)
                .build();

        RecipeIngredient milk1 = RecipeIngredient.builder()
                .id(1L)
                .recipe(recipe1)
                .ingredient(milk)
                .quantity(new BigDecimal("800"))
                .unit(Unit.ML)
                .build();

        RecipeIngredient milk2 = RecipeIngredient.builder()
                .id(2L)
                .recipe(recipe2)
                .ingredient(milk)
                .quantity(new BigDecimal("800"))
                .unit(Unit.ML)
                .build();

        RecipeIngredient milk3 = RecipeIngredient.builder()
                .id(3L)
                .recipe(recipe3)
                .ingredient(milk)
                .quantity(new BigDecimal("1"))
                .unit(Unit.L)
                .build();

        when(mealPlanRecipeRepository.findByMealPlanId(1L))
                .thenReturn(List.of(meal1, meal2, meal3));

        when(recipeIngredientRepository.findByRecipeId(1L))
                .thenReturn(List.of(milk1));

        when(recipeIngredientRepository.findByRecipeId(2L))
                .thenReturn(List.of(milk2));

        when(recipeIngredientRepository.findByRecipeId(3L))
                .thenReturn(List.of(milk3));

        List<ShoppingListItemResponse> result =
                shoppingListService.generateShoppingList(1L);

        assertEquals(1, result.size());

        ShoppingListItemResponse item = result.get(0);

        assertEquals(1L, item.getIngredientId());
        assertEquals("Milk", item.getIngredientName());
        assertEquals(0, new BigDecimal("2.6").compareTo(item.getQuantity()));
        assertEquals(Unit.L, item.getUnit());
        assertEquals(1L, item.getIngredientCategoryId());
        assertEquals("Dairy", item.getIngredientCategoryName());

        verify(mealPlanRecipeRepository).findByMealPlanId(1L);
        verify(recipeIngredientRepository).findByRecipeId(1L);
        verify(recipeIngredientRepository).findByRecipeId(2L);
        verify(recipeIngredientRepository).findByRecipeId(3L);

    }

    @Test
    void shouldThrowNotFoundWhenMealPlanDoesNotExist() {

        when(mealPlanRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> shoppingListService.generateShoppingList(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Meal plan not found: 999", exception.getReason());

        verify(mealPlanRepository).findById(999L);

    }

    private Recipe createRecipe() {

        return Recipe.builder()
                .id(1L)
                .name("Pancakes")
                .servings(4)
                .build();

    }

    private MealPlan createMealPlan() {

        return MealPlan.builder()
                .id(1L)
                .name("Family Meals")
                .build();

    }

    private MealPlanRecipe createMealPlanRecipe(MealPlan mealPlan, Recipe recipe) {

        return MealPlanRecipe.builder()
                .id(1L)
                .mealPlan(mealPlan)
                .recipe(recipe)
                .mealDate(LocalDate.of(2026, 8, 18))
                .mealType(MealType.DINNER)
                .servings(4)
                .build();

    }

    private Ingredient createIngredient() {

        IngredientCategory category = IngredientCategory.builder()
                .id(1L)
                .name("Pantry")
                .build();

        return Ingredient.builder()
                .id(1L)
                .name("Flour")
                .defaultUnit(Unit.G)
                .ingredientCategory(category)
                .build();

    }

    private RecipeIngredient createRecipeIngredient(Recipe recipe, Ingredient ingredient) {

        return RecipeIngredient.builder()
                .id(1L)
                .recipe(recipe)
                .ingredient(ingredient)
                .quantity(new BigDecimal("500"))
                .unit(Unit.G)
                .build();
    }

}
