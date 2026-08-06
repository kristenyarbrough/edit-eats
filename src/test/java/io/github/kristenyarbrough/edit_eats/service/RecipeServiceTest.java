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

        request.setIngredients(List.of(createIngredientRequest()));
        request.setSteps(List.of(createStepRequest()));
        request.setCategories(List.of(createCategoryRequest()));

        when(ingredientRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =  assertThrows(
                ResponseStatusException.class,
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

        Recipe recipe = createRecipe(request, true);

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

        List<RecipeIngredient> savedIngredients = captureSavedIngredients();

        assertEquals(1, savedIngredients.size());

        RecipeIngredient savedIngredient = savedIngredients.get(0);

        assertAll("recipe ingredient",
                () -> assertEquals(new BigDecimal("4"), savedIngredient.getQuantity()),
                () -> assertEquals(Unit.EACH, savedIngredient.getUnit()),
                () -> assertFalse(savedIngredient.getOptional()),
                () -> assertEquals(ingredient, savedIngredient.getIngredient()),
                () -> assertEquals(recipe, savedIngredient.getRecipe())
        );

        List<RecipeStep> savedSteps = captureSavedSteps();

        assertEquals(1, savedSteps.size());

        RecipeStep savedStep = savedSteps.get(0);

        assertAll("recipe step",
                () -> assertEquals(1, savedStep.getStepNumber()),
                () -> assertEquals("Whisk eggs.", savedStep.getInstruction()),
                () -> assertEquals(recipe, savedStep.getRecipe())
        );

        List<RecipeCategoryAssignment> assignments = captureSavedCategories();

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

    @Test
    void shouldCreateRecipeWithMultipleIngredients() {

        CreateRecipeRequest request = createValidRequest();

        Ingredient eggs = createIngredient();

        Ingredient milk = Ingredient.builder()
                .id(2L)
                .name("Milk")
                .defaultUnit(Unit.ML)
                .build();

        request.setIngredients(List.of(
                createIngredientRequest(1L, "4", Unit.EACH),
                createIngredientRequest(2L, "50", Unit.ML)));

        request.setSteps(List.of(createStepRequest()));

        RecipeCategory category = createCategory();

        request.setCategories(List.of(createCategoryRequest()));

        when(ingredientRepository.findById(1L))
                .thenReturn(Optional.of(eggs));

        when(ingredientRepository.findById(2L))
                .thenReturn(Optional.of(milk));

        when(recipeCategoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        Recipe recipe = createRecipe(request, true);

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

        List<RecipeIngredient> savedIngredients = captureSavedIngredients();

        assertEquals(2, savedIngredients.size());

        RecipeIngredient first = savedIngredients.get(0);
        RecipeIngredient second = savedIngredients.get(1);

        assertAll("first ingredient",
                () -> assertEquals(eggs, first.getIngredient()),
                () -> assertEquals(new BigDecimal("4"), first.getQuantity()),
                () -> assertEquals(Unit.EACH, first.getUnit())
        );

        assertAll("second ingredient",
                () -> assertEquals(milk, second.getIngredient()),
                () -> assertEquals(new BigDecimal("50"), second.getQuantity()),
                () -> assertEquals(Unit.ML, second.getUnit())
        );

        verify(ingredientRepository).findById(1L);
        verify(ingredientRepository).findById(2L);

        List<RecipeStep> savedSteps = captureSavedSteps();

        assertEquals(1, savedSteps.size());

        RecipeStep savedStep = savedSteps.get(0);

        assertAll("recipe step",
                () -> assertEquals(1, savedStep.getStepNumber()),
                () -> assertEquals("Whisk eggs.", savedStep.getInstruction()),
                () -> assertEquals(recipe, savedStep.getRecipe())
        );

        List<RecipeCategoryAssignment> assignments = captureSavedCategories();

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

    @Test
    void shouldCreateRecipeWithMultipleSteps() {

        CreateRecipeRequest request = createValidRequest();

        Ingredient ingredient = createIngredient();

        request.setIngredients(List.of(createIngredientRequest()));

        request.setSteps(List.of(
                createStepRequest("Whisk eggs."),
                createStepRequest("Heat pan."),
                createStepRequest("Cook gently.")
        ));

        RecipeCategory category = createCategory();

        request.setCategories(List.of(createCategoryRequest()));

        when(ingredientRepository.findById(1L))
                .thenReturn(Optional.of(ingredient));

        when(recipeCategoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        Recipe recipe = createRecipe(request, true);

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

        List<RecipeIngredient> savedIngredients = captureSavedIngredients();

        assertEquals(1, savedIngredients.size());

        RecipeIngredient savedIngredient = savedIngredients.get(0);

        assertAll("recipe ingredient",
                () -> assertEquals(new BigDecimal("4"), savedIngredient.getQuantity()),
                () -> assertEquals(Unit.EACH, savedIngredient.getUnit()),
                () -> assertFalse(savedIngredient.getOptional()),
                () -> assertEquals(ingredient, savedIngredient.getIngredient()),
                () -> assertEquals(recipe, savedIngredient.getRecipe())
        );

        List<RecipeStep> savedSteps = captureSavedSteps();

        assertEquals(3, savedSteps.size());

        RecipeStep savedStep1 = savedSteps.get(0);
        RecipeStep savedStep2 = savedSteps.get(1);
        RecipeStep savedStep3 = savedSteps.get(2);

        assertAll("recipe step 1",
                () -> assertEquals(1, savedStep1.getStepNumber()),
                () -> assertEquals("Whisk eggs.", savedStep1.getInstruction()),
                () -> assertEquals(recipe, savedStep1.getRecipe())
        );

        assertAll("recipe step 2",
                () -> assertEquals(2, savedStep2.getStepNumber()),
                () -> assertEquals("Heat pan.", savedStep2.getInstruction()),
                () -> assertEquals(recipe, savedStep2.getRecipe())
        );

        assertAll("recipe step 3",
                () -> assertEquals(3, savedStep3.getStepNumber()),
                () -> assertEquals("Cook gently.", savedStep3.getInstruction()),
                () -> assertEquals(recipe, savedStep3.getRecipe())
        );

        List<RecipeCategoryAssignment> assignments = captureSavedCategories();

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

    @Test
    void shouldCreateRecipeWithMultipleCategories() {

        CreateRecipeRequest request = createValidRequest();

        Ingredient ingredient = createIngredient();

        request.setIngredients(List.of(createIngredientRequest()));

        request.setSteps(List.of(createStepRequest()));

        RecipeCategory breakfast = createCategory();

        RecipeCategory vege = RecipeCategory.builder()
                .id(2L)
                .name("Vegetarian")
                .build();

        RecipeCategory quickMeals = RecipeCategory.builder()
                .id(3L)
                .name("Quick Meals")
                .build();

        request.setCategories(List.of(
                createCategoryRequest(1L),
                createCategoryRequest(2L),
                createCategoryRequest(3L)
        ));

        when(ingredientRepository.findById(1L))
                .thenReturn(Optional.of(ingredient));

        when(recipeCategoryRepository.findById(1L))
                .thenReturn(Optional.of(breakfast));

        when(recipeCategoryRepository.findById(2L))
                .thenReturn(Optional.of(vege));

        when(recipeCategoryRepository.findById(3L))
                .thenReturn(Optional.of(quickMeals));

        Recipe recipe = createRecipe(request, true);

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
        verify(recipeCategoryRepository).findById(2L);
        verify(recipeCategoryRepository).findById(3L);

        List<RecipeIngredient> savedIngredients = captureSavedIngredients();

        assertEquals(1, savedIngredients.size());

        RecipeIngredient savedIngredient = savedIngredients.get(0);

        assertAll("recipe ingredient",
                () -> assertEquals(new BigDecimal("4"), savedIngredient.getQuantity()),
                () -> assertEquals(Unit.EACH, savedIngredient.getUnit()),
                () -> assertFalse(savedIngredient.getOptional()),
                () -> assertEquals(ingredient, savedIngredient.getIngredient()),
                () -> assertEquals(recipe, savedIngredient.getRecipe())
        );

        List<RecipeStep> savedSteps = captureSavedSteps();

        assertEquals(1, savedSteps.size());

        RecipeStep savedStep = savedSteps.get(0);

        assertAll("recipe step",
                () -> assertEquals(1, savedStep.getStepNumber()),
                () -> assertEquals("Whisk eggs.", savedStep.getInstruction()),
                () -> assertEquals(recipe, savedStep.getRecipe())
        );

        List<RecipeCategoryAssignment> assignments = captureSavedCategories();

        assertEquals(3, assignments.size());

        RecipeCategoryAssignment assignBreak = assignments.get(0);
        RecipeCategoryAssignment assignVege = assignments.get(1);
        RecipeCategoryAssignment assignQm = assignments.get(2);

        assertAll("category assignment 1",
                () -> assertEquals(breakfast, assignBreak.getRecipeCategory()),
                () -> assertEquals(recipe, assignBreak.getRecipe())
        );

        assertAll("category assignment 2",
                () -> assertEquals(vege, assignVege.getRecipeCategory()),
                () -> assertEquals(recipe, assignVege.getRecipe())
        );

        assertAll("category assignment 3",
                () -> assertEquals(quickMeals, assignQm.getRecipeCategory()),
                () -> assertEquals(recipe, assignQm.getRecipe())
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
    @Test
    void shouldCreateRecipeWithoutCategories() {
        CreateRecipeRequest request = createValidRequest();

                Ingredient ingredient = createIngredient();

                request.setIngredients(List.of(createIngredientRequest()));

                request.setSteps(List.of(createStepRequest()));

                when(ingredientRepository.findById(1L))
                        .thenReturn(Optional.of(ingredient));

                Recipe recipe = createRecipe(request, false);

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

                List<RecipeIngredient> savedIngredients = captureSavedIngredients();

                assertEquals(1, savedIngredients.size());

                RecipeIngredient savedIngredient = savedIngredients.get(0);

                assertAll("recipe ingredient",
                        () -> assertEquals(new BigDecimal("4"), savedIngredient.getQuantity()),
                        () -> assertEquals(Unit.EACH, savedIngredient.getUnit()),
                        () -> assertFalse(savedIngredient.getOptional()),
                        () -> assertEquals(ingredient, savedIngredient.getIngredient()),
                        () -> assertEquals(recipe, savedIngredient.getRecipe())
                );

                List<RecipeStep> savedSteps = captureSavedSteps();

                assertEquals(1, savedSteps.size());

                RecipeStep savedStep = savedSteps.get(0);

                assertAll("recipe step",
                        () -> assertEquals(1, savedStep.getStepNumber()),
                        () -> assertEquals("Whisk eggs.", savedStep.getInstruction()),
                        () -> assertEquals(recipe, savedStep.getRecipe())
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

    @Test
    void shouldThrowWhenRecipeHasNoIngredients() {

        CreateRecipeRequest request = createValidRequest();

        ResponseStatusException exception =  assertThrows(ResponseStatusException.class,
                () -> recipeService.createRecipe(request));

        assertEquals("Recipe must contain at least one ingredient", exception.getReason());

        verifyNoInteractions(ingredientRepository);
        verify(recipeIngredientRepository, never()).saveAll(any());

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

    private void mockSaveRepositories() {

        when(recipeIngredientRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(recipeStepRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

    }

    private void mockCategoryRepositorySave() {

        when(recipeCategoryAssignmentRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

    }

    private CreateRecipeIngredientRequest createIngredientRequest(
            Long ingredientId, String quantity, Unit unit
    ) {

        CreateRecipeIngredientRequest request = new CreateRecipeIngredientRequest();
        request.setIngredientId(ingredientId);
        request.setQuantity(new BigDecimal(quantity));
        request.setUnit(unit);
        request.setOptional(false);

        return request;

    }

    private CreateRecipeStepRequest createStepRequest(
            String instruction
    ) {

        CreateRecipeStepRequest request = new CreateRecipeStepRequest();
        request.setInstruction(instruction);

        return request;

    }

    private CreateRecipeCategoryRequest createCategoryRequest(
            Long categoryId
    ) {

        CreateRecipeCategoryRequest request = new CreateRecipeCategoryRequest();
        request.setRecipeCategoryId(categoryId);

        return request;

    }

    private Recipe createRecipe(CreateRecipeRequest request, boolean withCategories) {

        when(recipeRepository.save(any(Recipe.class)))
                .thenAnswer(invocation -> {
                    Recipe recipe = invocation.getArgument(0);
                    recipe.setId(1L);
                    return recipe;
                });

        mockSaveRepositories();

        if (withCategories) {

            mockCategoryRepositorySave();

        }

        return recipeService.createRecipe(request);

    }

    private List<RecipeIngredient> captureSavedIngredients() {

        ArgumentCaptor<Iterable<RecipeIngredient>> ingredientCaptor =
                ArgumentCaptor.forClass(Iterable.class);

        verify(recipeIngredientRepository).saveAll(ingredientCaptor.capture());

        List<RecipeIngredient> list = new ArrayList<>();
        ingredientCaptor.getValue().forEach(list::add);

        return list;

    }

    private List<RecipeStep> captureSavedSteps() {

        ArgumentCaptor<Iterable<RecipeStep>> stepCaptor =
                ArgumentCaptor.forClass(Iterable.class);

        verify(recipeStepRepository).saveAll(stepCaptor.capture());

        List<RecipeStep> list = new ArrayList<>();
        stepCaptor.getValue().forEach(list::add);

        return list;

    }

    private List<RecipeCategoryAssignment> captureSavedCategories() {

        ArgumentCaptor<Iterable<RecipeCategoryAssignment>> categoryCaptor =
                ArgumentCaptor.forClass(Iterable.class);

        verify(recipeCategoryAssignmentRepository).saveAll(categoryCaptor.capture());

        List<RecipeCategoryAssignment> list = new ArrayList<>();
        categoryCaptor.getValue().forEach(list::add);

        return list;

    }

}
