package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.*;
import io.github.kristenyarbrough.edit_eats.dto.request.*;
import io.github.kristenyarbrough.edit_eats.dto.response.*;
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
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeIngredientSectionRepository recipeIngredientSectionRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final RecipeInstructionSectionRepository recipeInstructionSectionRepository;
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
                .passiveMinutes(request.getPassiveMinutes() == null
                        ? 0
                        : request.getPassiveMinutes())
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

        if (request.getIngredientSections() != null) {

            saveIngredientSections(recipe, request.getIngredientSections(), null);

        }

        if (request.getInstructionSections() != null) {

            saveInstructionSections(recipe, request.getInstructionSections(), null, steps.size() + 1);

        }

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

        List<RecipeIngredientSection> ingredientSections =
                recipeIngredientSectionRepository.findByRecipeIdOrderBySortOrder(recipeId);

        List<RecipeStep> recipeSteps = recipeStepRepository.findByRecipeIdOrderByStepNumber(recipeId);

        List<RecipeInstructionSection> instructionSections =
                recipeInstructionSectionRepository.findByRecipeIdOrderBySortOrder(recipeId);

        List<RecipeCategoryAssignment> categoryAssignments = recipeCategoryAssignmentRepository.findByRecipeId(recipeId);

        List<RecipeIngredientResponse> ingredientResponses =
                recipeIngredients.stream()
                        .filter(recipeIngredient -> recipeIngredient.getSection() == null)
                        .map(recipeIngredient -> RecipeIngredientResponse.builder()
                                .ingredientId(recipeIngredient.getIngredient().getId())
                                .ingredientName(recipeIngredient.getIngredient().getName())
                                .quantity(recipeIngredient.getQuantity())
                                .unit(recipeIngredient.getUnit())
                                .preparation(recipeIngredient.getPreparation())
                                .optional(recipeIngredient.getOptional())
                                .build())
                        .toList();

        List<RecipeIngredientSectionResponse> ingredientSectionResponses =
                buildIngredientSections(ingredientSections, recipeIngredients);

        List<RecipeStepResponse> stepResponses =
                recipeSteps.stream()
                        .filter(step -> step.getSection() == null)
                        .map(step -> RecipeStepResponse.builder()
                                .stepNumber(step.getStepNumber())
                                .instruction(step.getInstruction())
                                .build())
                        .toList();

        List<RecipeInstructionSectionResponse> instructionSectionResponses =
                buildInstructionSections(instructionSections, recipeSteps);

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
                .passiveMinutes(recipe.getPassiveMinutes())
                .activeMinutes(recipe.getActiveMinutes())
                .totalMinutes(recipe.getTotalMinutes())
                .formattedTotalTime(recipe.getFormattedTotalTime())
                .formattedActiveTime(recipe.getFormattedActiveTime())
                .servings(recipe.getServings())
                .difficulty(recipe.getDifficulty())
                .sourceUrl(recipe.getSourceUrl())
                .imageUrl(recipe.getImageUrl())
                .storageInstructions(recipe.getStorageInstructions())
                .freezerInstructions(recipe.getFreezerInstructions())
                .createdAt(recipe.getCreatedAt())
                .lastModifiedAt(recipe.getLastModifiedAt())
                .ingredients(ingredientResponses)
                .ingredientSections(ingredientSectionResponses)
                .steps(stepResponses)
                .instructionSections(instructionSectionResponses)
                .categories(categoryResponses)
                .build();
    }

    @Transactional
    public Recipe updateRecipe(Long id, UpdateRecipeRequest request) {

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Recipe not found: " + id));

        recipe.setName(request.getName());
        recipe.setPrepMinutes(request.getPrepMinutes());
        recipe.setCookMinutes(request.getCookMinutes());
        recipe.setPassiveMinutes(request.getPassiveMinutes() == null
                ? 0
                : request.getPassiveMinutes());
        recipe.setServings(request.getServings());
        recipe.setDifficulty(request.getDifficulty());
        recipe.setSourceUrl(request.getSourceUrl());
        recipe.setImageUrl(request.getImageUrl());
        recipe.setStorageInstructions(request.getStorageInstructions());
        recipe.setFreezerInstructions(request.getFreezerInstructions());
        recipe.setLastModifiedAt(LocalDateTime.now());

        List<Ingredient> ingredients = new ArrayList<>();

        for (CreateRecipeIngredientRequest ingredientRequest : request.getIngredients()) {

            Ingredient ingredient = ingredientRepository.findById(ingredientRequest.getIngredientId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Ingredient not found: " + ingredientRequest.getIngredientId()
            ));

            ingredients.add(ingredient);

        }

        recipeIngredientRepository.deleteByRecipeId(id);

        for (int i = 0; i < request.getIngredients().size(); i++) {

            CreateRecipeIngredientRequest ingredientRequest = request.getIngredients().get(i);

            RecipeIngredient recipeIngredient = RecipeIngredient.builder()
                    .recipe(recipe)
                    .ingredient(ingredients.get(i))
                    .quantity(ingredientRequest.getQuantity())
                    .unit(ingredientRequest.getUnit())
                    .preparation(ingredientRequest.getPreparation())
                    .optional(ingredientRequest.getOptional())
                    .build();

            recipeIngredientRepository.save(recipeIngredient);

        }

        recipeStepRepository.deleteByRecipeId(id);

        List<CreateRecipeStepRequest> steps = request.getSteps();

        for (int i = 0; i < steps.size(); i++) {

            CreateRecipeStepRequest stepRequest = steps.get(i);

            RecipeStep recipeStep = RecipeStep.builder()
                    .recipe(recipe)
                    .stepNumber(i + 1)
                    .instruction(stepRequest.getInstruction())
                    .build();

            recipeStepRepository.save(recipeStep);

        }

        List<RecipeCategory> categories = new ArrayList<>();

        for (CreateRecipeCategoryRequest categoryRequest : request.getCategories()) {

            RecipeCategory category = recipeCategoryRepository.findById(
                    categoryRequest.getRecipeCategoryId()
            ).orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Recipe category not found: " + categoryRequest.getRecipeCategoryId()
            ));

            categories.add(category);

        }

        recipeCategoryAssignmentRepository.deleteByRecipeId(id);

        for (int i = 0; i < request.getCategories().size(); i++) {

            CreateRecipeCategoryRequest categoryRequest = request.getCategories().get(i);

            RecipeCategoryAssignment assignment = RecipeCategoryAssignment.builder()
                    .recipe(recipe)
                    .recipeCategory(categories.get(i))
                    .build();

            recipeCategoryAssignmentRepository.save(assignment);

        }

        return recipeRepository.save(recipe);

    }

    @Transactional
    public void deleteRecipe(Long id) {

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Recipe not found: " + id));

        recipeIngredientRepository.deleteByRecipeId(id);
        recipeStepRepository.deleteByRecipeId(id);
        recipeCategoryAssignmentRepository.deleteByRecipeId(id);

        recipeRepository.delete(recipe);

    }

    @Transactional(readOnly = true)
    public List<RecipeSummaryResponse> findRecipeSummaries(String name) {

        List<Recipe> recipes = recipeRepository.findByNameContainingIgnoreCase(name);

        return recipes.stream()
                .map(recipe -> RecipeSummaryResponse.builder()
                        .id(recipe.getId())
                        .name(recipe.getName())
                        .prepMinutes(recipe.getPrepMinutes())
                        .cookMinutes(recipe.getCookMinutes())
                        .passiveMinutes(recipe.getPassiveMinutes())
                        .activeMinutes(recipe.getActiveMinutes())
                        .totalMinutes(recipe.getTotalMinutes())
                        .formattedTotalTime(recipe.getFormattedTotalTime())
                        .formattedActiveTime(recipe.getFormattedActiveTime())
                        .servings(recipe.getServings())
                        .difficulty(recipe.getDifficulty())
                        .imageUrl(recipe.getImageUrl())
                        .categories(
                                recipeCategoryAssignmentRepository
                                        .findByRecipeId(recipe.getId())
                                        .stream()
                                        .map(assignment ->
                                                RecipeCategoryResponse.builder()
                                                        .id(assignment.getRecipeCategory().getId())
                                                        .name(assignment.getRecipeCategory().getName())
                                                        .build())
                                        .toList()
                        )
                        .build())
                .toList();

    }

    @Transactional(readOnly = true)
    public List<RecipeResponse> findRecipes(String name) {

        return recipeRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(recipe -> RecipeResponse.builder()
                        .id(recipe.getId())
                        .name(recipe.getName())
                        .prepMinutes(recipe.getPrepMinutes())
                        .cookMinutes(recipe.getCookMinutes())
                        .passiveMinutes(recipe.getPassiveMinutes())
                        .activeMinutes(recipe.getActiveMinutes())
                        .totalMinutes(recipe.getTotalMinutes())
                        .formattedTotalTime(recipe.getFormattedTotalTime())
                        .formattedActiveTime(recipe.getFormattedActiveTime())
                        .servings(recipe.getServings())
                        .difficulty(recipe.getDifficulty())
                        .sourceUrl(recipe.getSourceUrl())
                        .imageUrl(recipe.getImageUrl())
                        .storageInstructions(recipe.getStorageInstructions())
                        .freezerInstructions(recipe.getFreezerInstructions())
                        .createdAt(recipe.getCreatedAt())
                        .lastModifiedAt(recipe.getLastModifiedAt())
                        .build())
                .toList();

    }

    private void saveIngredientSections(
            Recipe recipe,
            List<CreateRecipeIngredientSectionRequest> sectionRequests,
            RecipeIngredientSection parentSection) {

        for (int i = 0; i < sectionRequests.size(); i++) {

            CreateRecipeIngredientSectionRequest sectionRequest = sectionRequests.get(i);

            RecipeIngredientSection section = RecipeIngredientSection.builder()
                    .recipe(recipe)
                    .name(sectionRequest.getName())
                    .parentSection(parentSection)
                    .sortOrder(i)
                    .build();

            section = recipeIngredientSectionRepository.save(section);

            // Save ingredients belonging directly to this section
            for (CreateRecipeIngredientRequest ingredientRequest :
                sectionRequest.getIngredients()) {

                Ingredient ingredient = ingredientRepository.findById(
                        ingredientRequest.getIngredientId()
                ).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Ingredient not found: " + ingredientRequest.getIngredientId()
                ));

                RecipeIngredient recipeIngredient = RecipeIngredient.builder()
                        .recipe(recipe)
                        .ingredient(ingredient)
                        .quantity(ingredientRequest.getQuantity())
                        .unit(ingredientRequest.getUnit())
                        .preparation(ingredientRequest.getPreparation())
                        .optional(ingredientRequest.getOptional())
                        .section(section)
                        .build();

                recipeIngredientRepository.save(recipeIngredient);

            }

            // Recursively save any child sections
            saveIngredientSections(recipe, sectionRequest.getSections(), section);

        }

    }

    private int saveInstructionSections(
            Recipe recipe,
            List<CreateRecipeInstructionSectionRequest> sectionRequests,
            RecipeInstructionSection parentSection,
            int nextStepNumber) {

        for (int i = 0; i < sectionRequests.size(); i++) {

            CreateRecipeInstructionSectionRequest sectionRequest = sectionRequests.get(i);

            RecipeInstructionSection section = RecipeInstructionSection.builder()
                    .recipe(recipe)
                    .name(sectionRequest.getName())
                    .parentSection(parentSection)
                    .sortOrder(i)
                    .build();

            section = recipeInstructionSectionRepository.save(section);

            // Save instructions belonging directly to this section
            for (CreateRecipeStepRequest stepRequest :
                    sectionRequest.getSteps()) {

                RecipeStep recipeStep = RecipeStep.builder()
                        .recipe(recipe)
                        .stepNumber(nextStepNumber++)
                        .instruction(stepRequest.getInstruction())
                        .section(section)
                        .build();

                recipeStepRepository.save(recipeStep);

            }

            // Recursively save any child sections
            nextStepNumber = saveInstructionSections(recipe,
                    sectionRequest.getSections(),
                    section,
                    nextStepNumber);

        }

        return nextStepNumber;

    }

    private List<RecipeIngredientSectionResponse> buildIngredientSections(
            List<RecipeIngredientSection> sections,
            List<RecipeIngredient> recipeIngredients) {

        return sections.stream()
                .filter(section -> section.getParentSection() == null)
                .sorted(Comparator.comparing(RecipeIngredientSection::getSortOrder))
                .map(section ->
                        buildIngredientSections(section, sections, recipeIngredients))
                .toList();

    }

    private RecipeIngredientSectionResponse buildIngredientSections(
            RecipeIngredientSection section,
            List<RecipeIngredientSection> allSections,
            List<RecipeIngredient> recipeIngredients) {

        List<RecipeIngredientResponse> ingredients = recipeIngredients.stream()
                .filter(ingredient -> ingredient.getSection() != null)
                .filter(ingredient ->
                        ingredient.getSection().getId().equals(section.getId()))
                .map(recipeIngredient -> RecipeIngredientResponse.builder()
                        .ingredientId(recipeIngredient.getIngredient().getId())
                        .ingredientName(recipeIngredient.getIngredient().getName())
                        .quantity(recipeIngredient.getQuantity())
                        .unit(recipeIngredient.getUnit())
                        .preparation(recipeIngredient.getPreparation())
                        .optional(recipeIngredient.getOptional())
                        .build())
                .toList();

        List<RecipeIngredientSectionResponse> childSections = allSections.stream()
                .filter(child -> child.getParentSection() != null)
                .filter(child -> child.getParentSection().getId().equals(section.getId()))
                .sorted(Comparator.comparing(RecipeIngredientSection::getSortOrder))
                .map(child -> buildIngredientSections(child, allSections, recipeIngredients))
                .toList();

        return RecipeIngredientSectionResponse.builder()
                .name(section.getName())
                .ingredients(ingredients)
                .sections(childSections)
                .build();

    }

    private List<RecipeInstructionSectionResponse> buildInstructionSections(
            List<RecipeInstructionSection> sections,
            List<RecipeStep> steps) {

        return sections.stream()
                .filter(section -> section.getParentSection() == null)
                .sorted(Comparator.comparing(RecipeInstructionSection::getSortOrder))
                .map(section ->
                        buildInstructionSections(section, sections, steps))
                .toList();

    }

    private RecipeInstructionSectionResponse buildInstructionSections(
            RecipeInstructionSection section,
            List<RecipeInstructionSection> sections,
            List<RecipeStep> steps) {

        List<RecipeStepResponse> sectionSteps = steps.stream()
                .filter(step -> step.getSection() == section)
                .map(step -> RecipeStepResponse.builder()
                        .stepNumber(step.getStepNumber())
                        .instruction(step.getInstruction())
                        .build())
                .toList();

        List<RecipeInstructionSectionResponse> childSections = sections.stream()
                .filter(child -> child.getParentSection() == section)
                .sorted(Comparator.comparing(RecipeInstructionSection::getSortOrder))
                .map(child -> buildInstructionSections(child, sections, steps))
                .toList();

        return RecipeInstructionSectionResponse.builder()
                .name(section.getName())
                .steps(sectionSteps)
                .sections(childSections)
                .build();

    }

}
//
//    @Transactional(readOnly = true)
//    public List<Recipe> getAll() {
//        return recipeRepository.findAllByOrderByCreatedAtDesc();
//    }
//
//
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

