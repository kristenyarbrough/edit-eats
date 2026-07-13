package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.*;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateRecipeCategoryRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateRecipeIngredientRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateRecipeRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateRecipeStepRequest;
import io.github.kristenyarbrough.edit_eats.repository.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
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

        CreateRecipeRequest request = createValidRequest();

        CreateRecipeIngredientRequest ingredientRequest = createIngredientRequest();

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

        CreateRecipeRequest request = createValidRequest();

        Ingredient ingredient = createIngredient();

        request.setIngredients(List.of(createIngredientRequest()));

        request.setSteps(List.of(createStepRequest()));

        RecipeCategory category = createCategory();

        request.setCategories(List.of(createCategoryRequest()));

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

        mockSaveAllRepositories();

        Recipe recipe = recipeService.createRecipe(request);

        assertAll("created recipe",
                () -> assertNotNull(recipe),
                () -> assertEquals(1L, recipe.getId()),
                () -> assertEquals("Scrambled Eggs", recipe.getName()),
                () -> assertEquals(2, recipe.getServings()),
                () -> assertEquals(Difficulty.EASY, recipe.getDifficulty())
        );

        ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);

        verify(recipeRepository).save(recipeCaptor.capture());

        Recipe savedRecipe = recipeCaptor.getValue();

        assertAll("recipe",
                () -> assertEquals("Scrambled Eggs", savedRecipe.getName()),
                () -> assertEquals(2, savedRecipe.getServings()),
                () -> assertEquals(Difficulty.EASY, savedRecipe.getDifficulty())
        );

        verify(ingredientRepository).findById(1L);
        verify(recipeCategoryRepository).findById(1L);

        ArgumentCaptor<Iterable<RecipeIngredient>> ingredientCaptor =
                ArgumentCaptor.forClass(Iterable.class);

        verify(recipeIngredientRepository).saveAll(ingredientCaptor.capture());

        List<RecipeIngredient> savedIngredients = new ArrayList<>();
        ingredientCaptor.getValue().forEach(savedIngredients::add);

        assertEquals(1, savedIngredients.size());

        RecipeIngredient savedIngredient = savedIngredients.get(0);

        assertAll("recipe ingredient",
                () -> assertEquals(new BigDecimal("4"), savedIngredient.getQuantity()),
                () -> assertEquals(Unit.EACH, savedIngredient.getUnit()),
                () -> assertFalse(savedIngredient.getOptional()),
                () -> assertEquals(ingredient, savedIngredient.getIngredient()),
                () -> assertEquals(recipe, savedIngredient.getRecipe())
        );

        verify(recipeStepRepository).saveAll(any());
        verify(recipeCategoryAssignmentRepository).saveAll(any());

        ArgumentCaptor<Iterable<RecipeStep>> stepCaptor =
                ArgumentCaptor.forClass(Iterable.class);

        verify(recipeStepRepository).saveAll(stepCaptor.capture());

        List<RecipeStep> savedSteps = new ArrayList<>();
        stepCaptor.getValue().forEach(savedSteps::add);

        assertEquals(1, savedSteps.size());

        RecipeStep savedStep = savedSteps.get(0);

        assertAll("recipe step",
                () -> assertEquals(1, savedStep.getStepNumber()),
                () -> assertEquals("Whisk eggs.", savedStep.getInstruction()),
                () -> assertEquals(recipe, savedStep.getRecipe())
        );

        ArgumentCaptor<Iterable<RecipeCategoryAssignment>> categoryCaptor =
                ArgumentCaptor.forClass(Iterable.class);

        verify(recipeCategoryAssignmentRepository).saveAll(categoryCaptor.capture());

        List<RecipeCategoryAssignment> assignments = new ArrayList<>();
        categoryCaptor.getValue().forEach(assignments::add);

        assertEquals(1, assignments.size());

        RecipeCategoryAssignment assignment = assignments.get(0);

        assertAll("category assignment",
                () -> assertEquals(category, assignment.getRecipeCategory()),
                () -> assertEquals(recipe, assignment.getRecipe())
        );

        verifyNoMoreInteractions(
                recipeRepository,
                ingredientRepository,
                recipeIngredientRepository,
                recipeStepRepository,
                recipeCategoryRepository,
                recipeCategoryAssignmentRepository
        );

    }

    private CreateRecipeRequest createValidRequest() {

        CreateRecipeRequest request = new CreateRecipeRequest();

        request.setName("Scrambled Eggs");
        request.setPrepMinutes(2);
        request.setCookMinutes(5);
        request.setServings(2);
        request.setDifficulty(Difficulty.EASY);

        return request;

    }

    private Ingredient createIngredient() {

        return Ingredient.builder()
                .id(1L)
                .name("Eggs")
                .defaultUnit(Unit.EACH)
                .build();

    }

    private RecipeCategory createCategory() {

        return RecipeCategory.builder()
                .id(1L)
                .name("Breakfast")
                .build();

    }

    private CreateRecipeIngredientRequest createIngredientRequest() {

        CreateRecipeIngredientRequest request = new CreateRecipeIngredientRequest();
        request.setIngredientId(1L);
        request.setQuantity(new BigDecimal("4"));
        request.setUnit(Unit.EACH);
        request.setOptional(false);

        return request;

    }

    private CreateRecipeStepRequest createStepRequest() {

        CreateRecipeStepRequest request = new CreateRecipeStepRequest();
        request.setInstruction("Whisk eggs.");

        return request;

    }

    private CreateRecipeCategoryRequest createCategoryRequest() {

        CreateRecipeCategoryRequest request = new CreateRecipeCategoryRequest();
        request.setRecipeCategoryId(1L);

        return request;
    }

    private void mockSaveAllRepositories() {

        when(recipeIngredientRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(recipeStepRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(recipeCategoryAssignmentRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

    }

    @Test
    void shouldThrowWhenRecipeCategoryDoesNotExist() {

        CreateRecipeRequest request = createValidRequest();

        request.setIngredients(List.of(createIngredientRequest()));
        request.setSteps(List.of(createStepRequest()));
        request.setCategories(List.of(createCategoryRequest()));

        Ingredient ingredient = createIngredient();

        when(ingredientRepository.findById(1L))
                .thenReturn(Optional.of(ingredient));

        when(recipeCategoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> recipeService.createRecipe(request)
        );

        assertEquals("Recipe category not found: 1", exception.getReason());

        verify(ingredientRepository).findById(1L);
        verify(recipeCategoryRepository).findById(1L);

        verify(recipeRepository, never()).save(any());
        verify(recipeIngredientRepository, never()).saveAll(any());
        verify(recipeStepRepository, never()).saveAll(any());
        verify(recipeCategoryAssignmentRepository, never()).saveAll(any());

    }
}
