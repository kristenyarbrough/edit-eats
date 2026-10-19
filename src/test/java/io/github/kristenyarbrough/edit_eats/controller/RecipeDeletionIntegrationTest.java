package io.github.kristenyarbrough.edit_eats.controller;

import io.github.kristenyarbrough.edit_eats.domain.*;
import io.github.kristenyarbrough.edit_eats.repository.RecipeIngredientSectionRepository;
import io.github.kristenyarbrough.edit_eats.repository.RecipeInstructionSectionRepository;
import io.github.kristenyarbrough.edit_eats.repository.RecipeRepository;
import io.github.kristenyarbrough.edit_eats.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class RecipeDeletionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeIngredientSectionRepository recipeIngredientSectionRepository;

    @Autowired
    private RecipeInstructionSectionRepository recipeInstructionSectionRepository;

    @Test
    void shouldDeleteRecipeWithNestedSections() throws Exception {

        User user = userRepository.findByUsername("development-user")
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .username("development-user")
                                .build()
        ));

        LocalDateTime now = LocalDateTime.now();

        Recipe recipe = recipeRepository.save(
                Recipe.builder()
                        .name("Recipe to delete")
                        .prepMinutes(10)
                        .cookMinutes(20)
                        .passiveMinutes(0)
                        .servings(4)
                        .difficulty(Difficulty.MEDIUM)
                        .createdAt(now)
                        .lastModifiedAt(now)
                        .user(user)
                        .build()
        );

        RecipeIngredientSection ingredientParent = recipeIngredientSectionRepository.save(
                RecipeIngredientSection.builder()
                        .recipe(recipe)
                        .name("Sauce")
                        .sortOrder(1)
                        .build()
        );

        recipeIngredientSectionRepository.save(
                RecipeIngredientSection.builder()
                        .recipe(recipe)
                        .name("Spices")
                        .parentSection(ingredientParent)
                        .sortOrder(2)
                        .build()
        );

        RecipeInstructionSection instructionParent = recipeInstructionSectionRepository.save(
                RecipeInstructionSection.builder()
                        .recipe(recipe)
                        .name("Prepare the sauce")
                        .sortOrder(1)
                        .build()
        );

        recipeInstructionSectionRepository.save(
                RecipeInstructionSection.builder()
                        .recipe(recipe)
                        .name("Mix the ingredients")
                        .parentSection(instructionParent)
                        .sortOrder(2)
                        .build()
        );

        assertEquals(2,
                recipeIngredientSectionRepository.findByRecipeIdOrderBySortOrder(recipe.getId()).size());

        assertEquals(2,
                recipeInstructionSectionRepository.findByRecipeIdOrderBySortOrder(recipe.getId()).size());

        mockMvc.perform(delete("/api/recipes/{id}", recipe.getId()))
                .andExpect(status().isNoContent());

        assertFalse(recipeRepository.existsById(recipe.getId()));

        assertTrue(recipeIngredientSectionRepository
                .findByRecipeIdOrderBySortOrder(recipe.getId()).isEmpty());

        assertTrue(recipeInstructionSectionRepository
                .findByRecipeIdOrderBySortOrder(recipe.getId()).isEmpty());

    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentRecipe() throws Exception {

        mockMvc.perform(delete("/api/recipes/{id}", 999999L))
                .andExpect(status().isNotFound());

    }

    @Test
    void shouldNotDeleteAnotherUsersRecipe() throws Exception {

        User otherUser = userRepository.save(
                User.builder()
                        .username("another-test-user")
                        .build()
        );

        LocalDateTime now = LocalDateTime.now();

        Recipe recipe = recipeRepository.save(
                Recipe.builder()
                        .name("Another Users Recipe")
                        .prepMinutes(10)
                        .cookMinutes(20)
                        .passiveMinutes(0)
                        .servings(4)
                        .difficulty(Difficulty.MEDIUM)
                        .createdAt(now)
                        .lastModifiedAt(now)
                        .user(otherUser)
                        .build()
        );

        mockMvc.perform(delete("/api/recipes/{id}", recipe.getId()))
                .andExpect(status().isNotFound());

        assertTrue(recipeRepository.existsById(recipe.getId()));

    }

}
