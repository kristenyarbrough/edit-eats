package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.*;
import io.github.kristenyarbrough.edit_eats.dto.request.AddMealPlanRecipeRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateMealPlanRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.UpdateMealPlanRecipeRequest;
import io.github.kristenyarbrough.edit_eats.dto.response.MealPlanRecipeResponse;
import io.github.kristenyarbrough.edit_eats.dto.response.MealPlanResponse;
import io.github.kristenyarbrough.edit_eats.repository.MealPlanRecipeRepository;
import io.github.kristenyarbrough.edit_eats.repository.MealPlanRepository;
import io.github.kristenyarbrough.edit_eats.repository.RecipeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealPlanServiceTest {

    @Mock
    private MealPlanRepository mealPlanRepository;

    @Mock
    private MealPlanRecipeRepository mealPlanRecipeRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private MealPlanService mealPlanService;

    @Test
    void shouldCreateMealPlan() {

        CreateMealPlanRequest request = createMealPlanRequest();

        MealPlan savedMealPlan = createMealPlan();

        when(mealPlanRepository.save(any(MealPlan.class)))
                .thenReturn(savedMealPlan);

        MealPlan mealPlan = mealPlanService.createMealPlan(request);

        assertEquals(1L, mealPlan.getId());
        assertEquals("Weekly Family Meals", mealPlan.getName());
        assertEquals(LocalDate.of(2026, 8, 3), mealPlan.getWeekStarting());

        verify(mealPlanRepository).save(any(MealPlan.class));

    }

    @Test
    void shouldGetMealPlan() {

        MealPlan mealPlan = createMealPlan();

        when(mealPlanRepository.findById(1L))
                .thenReturn(Optional.of(mealPlan));

        MealPlanResponse response = mealPlanService.getMealPlan(1L);

        assertEquals(1L, response.getId());
        assertEquals("Weekly Family Meals", response.getName());
        assertEquals(LocalDate.of(2026, 8, 3), response.getWeekStarting());

        verify(mealPlanRepository).findById(1L);

    }

    @Test
    void shouldThrowExceptionWhenMealPlanDoesNotExist() {

        when(mealPlanRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> mealPlanService.getMealPlan(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Meal plan not found: 99", exception.getReason());

        verify(mealPlanRepository).findById(99l);

    }

    @Test
    void shouldReturnAllMealPlans() {

        MealPlan first = createMealPlan();

        MealPlan second = MealPlan.builder()
                .id(2L)
                .name("Camping Weekend")
                .weekStarting(LocalDate.of(2026, 8, 10))
                .build();

        when(mealPlanRepository.findAll())
                .thenReturn(List.of(first, second));

        List<MealPlan> mealPlans = mealPlanService.getMealPlans();

        assertEquals(2, mealPlans.size());
        assertEquals("Weekly Family Meals", mealPlans.get(0).getName());
        assertEquals("Camping Weekend", mealPlans.get(1).getName());

        verify(mealPlanRepository).findAll();

    }

    @Test
    void shouldAddRecipeToMealPlan() {

        MealPlan mealPlan = createMealPlan();

        Recipe recipe = createRecipe();

        AddMealPlanRecipeRequest request = createMealPlanRecipeRequest();

        when(mealPlanRepository.findById(1L))
                .thenReturn(Optional.of(mealPlan));

        when(recipeRepository.findById(1L))
                .thenReturn(Optional.of(recipe));

        MealPlanRecipe savedMeal = MealPlanRecipe.builder()
                .id(1L)
                .mealPlan(mealPlan)
                .recipe(recipe)
                .mealDate(request.getMealDate())
                .mealType(request.getMealType())
                .servings(request.getServings())
                .build();

        when(mealPlanRecipeRepository.save(any(MealPlanRecipe.class)))
                .thenReturn(savedMeal);

        MealPlanRecipe result = mealPlanService.addRecipeToMealPlan(1L, request);

        assertAll(
                () -> assertEquals(1L, result.getId()),
                () -> assertEquals(recipe, result.getRecipe()),
                () -> assertEquals(mealPlan, result.getMealPlan()),
                () -> assertEquals(LocalDate.of(2026, 8, 3), result.getMealDate()),
                () -> assertEquals(MealType.DINNER, result.getMealType()),
                () -> assertEquals(4, result.getServings()));

        verify(mealPlanRepository).findById(1L);
        verify(recipeRepository).findById(1L);
        verify(mealPlanRecipeRepository).save(any(MealPlanRecipe.class));

    }

    @Test
    void shouldThrowWhenMealPlanDoesNotExist() {

        AddMealPlanRecipeRequest request = createMealPlanRecipeRequest();

        when(mealPlanRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> mealPlanService.addRecipeToMealPlan(99L, request)
        );

        assertEquals("Meal plan not found: 99", exception.getReason());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(mealPlanRepository).findById(99L);
        verifyNoInteractions(recipeRepository);
        verify(mealPlanRecipeRepository, never()).save(any());

    }

    @Test
    void shouldThrowWhenRecipeDoesNotExist() {

        MealPlan mealPlan = createMealPlan();

        AddMealPlanRecipeRequest request = createMealPlanRecipeRequest();

        when(mealPlanRepository.findById(1L))
                .thenReturn(Optional.of(mealPlan));

        when(recipeRepository.findById(99L))
                .thenReturn(Optional.empty());

        request.setRecipeId(99L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> mealPlanService.addRecipeToMealPlan(1L, request)
        );

        assertEquals("Recipe not found: 99", exception.getReason());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(mealPlanRepository).findById(1L);
        verify(recipeRepository).findById(99L);
        verify(mealPlanRecipeRepository, never()).save(any());

    }

    @Test
    void shouldGetMealPlanWithMeals() {

        MealPlan mealPlan = createMealPlan();
        Recipe recipe = createRecipe();

        MealPlanRecipe mealPlanRecipe = MealPlanRecipe.builder()
                .id(1L)
                .mealPlan(mealPlan)
                .recipe(recipe)
                .mealDate(LocalDate.of(2026, 8, 3))
                .mealType(MealType.DINNER)
                .servings(4)
                .build();

        when(mealPlanRepository.findById(1L))
                .thenReturn(Optional.of(mealPlan));

        when(mealPlanRecipeRepository.findByMealPlanIdOrderByMealDateAscMealTypeAsc(1L))
                .thenReturn(List.of(mealPlanRecipe));

        MealPlanResponse response = mealPlanService.getMealPlan(1L);

        assertEquals(1L, response.getId());
        assertEquals("Weekly Family Meals", response.getName());
        assertEquals(1, response.getMeals().size());

        MealPlanRecipeResponse meal = response.getMeals().get(0);

        assertEquals(1L, meal.getId());
        assertEquals(1L, meal.getRecipeId());
        assertEquals("Lasagne", meal.getRecipeName());
        assertEquals(LocalDate.of(2026, 8, 3), meal.getMealDate());
        assertEquals(MealType.DINNER, meal.getMealType());
        assertEquals(4, meal.getServings());

        verify(mealPlanRepository).findById(1L);
        verify(mealPlanRecipeRepository).findByMealPlanIdOrderByMealDateAscMealTypeAsc(1L);

    }

    @Test
    void shouldGetMealPlanWithNoMeals() {

        MealPlan mealPlan = createMealPlan();

        when(mealPlanRepository.findById(1L))
                .thenReturn(Optional.of(mealPlan));

        when(mealPlanRecipeRepository.findByMealPlanIdOrderByMealDateAscMealTypeAsc(1L))
                .thenReturn(List.of());

        MealPlanResponse response = mealPlanService.getMealPlan(1L);

        assertEquals(1L, response.getId());
        assertNotNull(response.getMeals());
        assertTrue(response.getMeals().isEmpty());

        verify(mealPlanRepository).findById(1L);
        verify(mealPlanRecipeRepository).findByMealPlanIdOrderByMealDateAscMealTypeAsc(1L);

    }

    @Test
    void shouldUpdateMealPlanRecipe() {

        MealPlan mealPlan = createMealPlan();
        Recipe existingRecipe = createRecipe();

        Recipe newRecipe = Recipe.builder()
                .id(2L)
                .name("Chicken Curry")
                .servings(4)
                .difficulty(Difficulty.EASY)
                .build();

        MealPlanRecipe mealPlanRecipe = MealPlanRecipe.builder()
                .id(1L)
                .mealPlan(mealPlan)
                .recipe(existingRecipe)
                .mealDate(LocalDate.of(2026, 8, 3))
                .mealType(MealType.DINNER)
                .servings(4)
                .build();

        UpdateMealPlanRecipeRequest request = new UpdateMealPlanRecipeRequest();

        request.setRecipeId(2L);
        request.setMealDate(LocalDate.of(2026, 8, 5));
        request.setMealType(MealType.LUNCH);
        request.setServings(6);

        when(mealPlanRecipeRepository.findById(1L))
                .thenReturn(Optional.of(mealPlanRecipe));

        when(recipeRepository.findById(2L))
                .thenReturn(Optional.of(newRecipe));

        when(mealPlanRecipeRepository.save(mealPlanRecipe))
                .thenReturn(mealPlanRecipe);

        MealPlanRecipe result = mealPlanService.updateMealPlanRecipe(1L, request);

        assertEquals(1L, result.getId());
        assertEquals(newRecipe, result.getRecipe());
        assertEquals(LocalDate.of(2026, 8, 5), result.getMealDate());
        assertEquals(MealType.LUNCH, result.getMealType());
        assertEquals(6, result.getServings());

        verify(mealPlanRecipeRepository).findById(1L);
        verify(recipeRepository).findById(2L);
        verify(mealPlanRecipeRepository).save(mealPlanRecipe);

        assertNotNull(mealPlan.getLastModifiedAt());

    }

    @Test
    void shouldThrowWhenMealPlanRecipeDoesNotExist() {

        UpdateMealPlanRecipeRequest request = createUpdateRequest();

        when(mealPlanRecipeRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> mealPlanService.updateMealPlanRecipe(99L, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Meal plan recipe not found: 99", exception.getReason());

        verify(mealPlanRecipeRepository).findById(99L);
        verifyNoInteractions(recipeRepository);

    }

    @Test
    void shouldThrowWhenUpdatedRecipeDoesNotExist() {

        MealPlan mealPlan = createMealPlan();
        Recipe existingRecipe = createRecipe();

        MealPlanRecipe mealPlanRecipe = MealPlanRecipe.builder()
                .id(1L)
                .mealPlan(mealPlan)
                .recipe(existingRecipe)
                .mealDate(LocalDate.of(2026, 8, 3))
                .mealType(MealType.DINNER)
                .servings(4)
                .build();

        UpdateMealPlanRecipeRequest request = createUpdateRequest();

        request.setRecipeId(99L);

        when(mealPlanRecipeRepository.findById(1L))
                .thenReturn(Optional.of(mealPlanRecipe));

        when(recipeRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> mealPlanService.updateMealPlanRecipe(1L, request)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        assertEquals(
                "Recipe not found: 99",
                exception.getReason()
        );

        verify(mealPlanRecipeRepository).findById(1L);
        verify(recipeRepository).findById(99L);
        verify(mealPlanRecipeRepository, never()).save(any());

    }

    private CreateMealPlanRequest createMealPlanRequest() {

        CreateMealPlanRequest request = new CreateMealPlanRequest();
        request.setName("Weekly Family Meals");
        request.setWeekStarting(LocalDate.of(2026, 8, 3));

        return request;

    }

    private MealPlan createMealPlan() {

        return MealPlan.builder()
                .id(1L)
                .name("Weekly Family Meals")
                .weekStarting(LocalDate.of(2026, 8, 3))
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

    }

    private Recipe createRecipe() {

        return Recipe.builder()
                .id(1L)
                .name("Lasagne")
                .servings(4)
                .difficulty(Difficulty.EASY)
                .build();

    }

    private AddMealPlanRecipeRequest createMealPlanRecipeRequest() {

        AddMealPlanRecipeRequest request = new AddMealPlanRecipeRequest();

        request.setRecipeId(1L);
        request.setMealDate(LocalDate.of(2026, 8, 3));
        request.setMealType(MealType.DINNER);
        request.setServings(4);

        return request;

    }

    private UpdateMealPlanRecipeRequest createUpdateRequest() {

        UpdateMealPlanRecipeRequest request = new UpdateMealPlanRecipeRequest();

        request.setRecipeId(2L);
        request.setMealDate(LocalDate.of(2026, 8, 5));
        request.setMealType(MealType.DINNER);
        request.setServings(4);

        return request;

    }

}
