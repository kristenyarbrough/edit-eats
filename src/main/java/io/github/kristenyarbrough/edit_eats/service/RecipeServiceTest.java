package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.*;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateRecipeCategoryRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateRecipeIngredientRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateRecipeRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateRecipeStepRequest;
import io.github.kristenyarbrough.edit_eats.repository.*;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
 class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private RecipeStepRepository recipeStepRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private RecipeIngredientRepository recipeIngredientRepository;

    @Mock
    private RecipeCategoryRepository recipeCategoryRepository;

    @Mock
    private RecipeCategoryAssignmentRepository recipeCategoryAssignmentRepository;

    @InjectMocks
    private RecipeService recipeService;

    @Test
    void shouldThrowWhenIngredientDoesNotExist() {

        CreateRecipeRequest request = new CreateRecipeRequest();

        request.setName("Toast");
        request.setPrepMinutes(2);
        request.setCookMinutes(3);
        request.setServings(1);
        request.setDifficulty(Difficulty.EASY);

        CreateRecipeIngredientRequest ingredientRequest = new CreateRecipeIngredientRequest();

        ingredientRequest.setIngredientId(1L);
        ingredientRequest.setQuantity(new BigDecimal("2"));
        ingredientRequest.setUnit(Unit.EACH);
        ingredientRequest.setPreparation(null);
        ingredientRequest.setOptional(false);

        request.setIngredients(List.of(ingredientRequest));

        when(ingredientRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =  assertThrows(ResponseStatusException.class,
                () -> recipeService.createRecipe(request));

        assertEquals("Ingredient not found: 1", exception.getReason());

        verify(ingredientRepository).findById(1L);
        verify(recipeIngredientRepository, never()).saveAll(any());
    }

    @Test
    void shouldCreateRecipe() {

        CreateRecipeRequest request = new CreateRecipeRequest();

        request.setName("Scrambled Eggs");
        request.setPrepMinutes(2);
        request.setCookMinutes(5);
        request.setServings(2);
        request.setDifficulty(Difficulty.EASY);

        Ingredient ingredient = Ingredient.builder()
                .id(1L)
                .name("Eggs")
                .defaultUnit(Unit.EACH)
                .build();

        CreateRecipeIngredientRequest ingredientRequest = new CreateRecipeIngredientRequest();

        ingredientRequest.setIngredientId(1L);
        ingredientRequest.setQuantity(new BigDecimal("4"));
        ingredientRequest.setUnit(Unit.EACH);
        ingredientRequest.setOptional(false);

        request.setIngredients(List.of(ingredientRequest));

        CreateRecipeStepRequest step = new CreateRecipeStepRequest();

        step.setInstruction("Whisk eggs.");

        request.setSteps(List.of(step));

        RecipeCategory category = RecipeCategory.builder()
                .id(1L)
                .name("Breakfast")
                .build();

        CreateRecipeCategoryRequest categoryRequest = new CreateRecipeCategoryRequest();

        categoryRequest.setRecipeCategoryId(1L);

        request.setCategories(List.of(categoryRequest));

        when(ingredientRepository.findById(1L))
                .thenReturn(Optional.of(ingredient));

        when(recipeCategoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(recipeRepository.save(any(Recipe.class)))
                .thenAnswer(invocation -> {
                    Recipe recipe = invocation.getArgument(0);
                    recipe.setId(1L);
                    return recipe;
                });

        when(recipeIngredientRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(recipeStepRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(recipeCategoryAssignmentRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Recipe recipe = recipeService.createRecipe(request);

        assertNotNull(recipe);
        assertEquals(1L, recipe.getId());
        assertEquals("Scrambled Eggs", recipe.getName());
        assertEquals(2, recipe.getServings());
        assertEquals(Difficulty.EASY, recipe.getDifficulty());

        verify(recipeRepository).save(any(Recipe.class));

        verify(ingredientRepository).findById(1L);
        verify(recipeCategoryRepository).findById(1L);

        verify(recipeIngredientRepository).saveAll(any());
        verify(recipeStepRepository).saveAll(any());
        verify(recipeCategoryAssignmentRepository).saveAll(any());

    }
}
