package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.RecipeCategory;
import io.github.kristenyarbrough.edit_eats.domain.User;
import io.github.kristenyarbrough.edit_eats.dto.response.RecipeCategoryResponse;
import io.github.kristenyarbrough.edit_eats.repository.RecipeCategoryRepository;
import io.github.kristenyarbrough.edit_eats.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeCategoryServiceTest {

    @Mock
    private RecipeCategoryRepository recipeCategoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RecipeCategoryService recipeCategoryService;

    @Test
    void shouldGetAvailableCategoriesForUser() {

        User user = createUser();

        RecipeCategory universalCategory = RecipeCategory.builder()
                .id(1L)
                .name("Dinner")
                .user(null)
                .build();

        RecipeCategory userCategory = RecipeCategory.builder()
                .id(2L)
                .name("Quick Meals")
                .user(user)
                .build();

        when(userRepository.findByUsername("development-user"))
                .thenReturn(Optional.of(user));

        when(recipeCategoryRepository
                .findAllByUserIdIsNullOrUserIdOrderByNameAsc(user.getId()))
                .thenReturn(List.of(universalCategory, userCategory));

        List<RecipeCategoryResponse> result = recipeCategoryService.getAvailableCategories();

        assertEquals(2, result.size());
        assertEquals("Dinner", result.get(0).getName());
        assertEquals("Quick Meals", result.get(1).getName());

        verify(recipeCategoryRepository)
                .findAllByUserIdIsNullOrUserIdOrderByNameAsc(user.getId());

    }

    @Test
    void shouldCreateUserCategory() {

        User user = createUser();

        when(userRepository.findByUsername("development-user"))
                .thenReturn(Optional.of(user));

        when(recipeCategoryRepository
                .existsAvailableCategoryForUser(user.getId(), "Quick Meals"))
                .thenReturn(false);

        RecipeCategory savedCategory = RecipeCategory.builder()
                .id(1L)
                .name("Quick Meals")
                .user(user)
                .build();

        when(recipeCategoryRepository.save(any(RecipeCategory.class)))
                .thenReturn(savedCategory);

        RecipeCategoryResponse result = recipeCategoryService.createCategory("Quick Meals");

        assertEquals(1L, result.getId());
        assertEquals("Quick Meals", result.getName());

        verify(recipeCategoryRepository).save(any(RecipeCategory.class));

    }

    @Test
    void shouldRejectDuplicateUserCategory() {

        User user = createUser();

        when(userRepository.findByUsername("development-user"))
                .thenReturn(Optional.of(user));

        when(recipeCategoryRepository
                .existsAvailableCategoryForUser(user.getId(), "Quick Meals"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeCategoryService.createCategory("Quick Meals")
        );

        assertEquals(
                "A recipe category with the name 'Quick Meals' already exists.",
                exception.getMessage()
        );

        verify(recipeCategoryRepository, never())
                .save(any(RecipeCategory.class));

    }

    @Test
    void shouldRejectUserCategoryWithSameNameAsUniversalCategory() {

        User user = createUser();

        when(userRepository.findByUsername("development-user"))
                .thenReturn(Optional.of(user));

        when(recipeCategoryRepository
                .existsAvailableCategoryForUser(user.getId(), "Breakfast"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeCategoryService.createCategory("Breakfast")
        );

        assertEquals(
                "A recipe category with the name 'Breakfast' already exists.",
                exception.getMessage()
        );

        verify(recipeCategoryRepository, never())
                .save(any(RecipeCategory.class));

    }

    private User createUser() {

        return User.builder()
                .id(1L)
                .username("development-user")
                .build();

    }

}
