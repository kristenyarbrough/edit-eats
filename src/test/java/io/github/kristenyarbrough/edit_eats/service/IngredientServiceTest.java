package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.Ingredient;
import io.github.kristenyarbrough.edit_eats.domain.IngredientCategory;
import io.github.kristenyarbrough.edit_eats.domain.Unit;
import io.github.kristenyarbrough.edit_eats.domain.User;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateIngredientRequest;
import io.github.kristenyarbrough.edit_eats.dto.response.IngredientResponse;
import io.github.kristenyarbrough.edit_eats.repository.IngredientCategoryRepository;
import io.github.kristenyarbrough.edit_eats.repository.IngredientRepository;
import io.github.kristenyarbrough.edit_eats.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    @Mock
    IngredientRepository ingredientRepository;

    @Mock
    IngredientCategoryRepository ingredientCategoryRepository;
    
    @Mock
    UserRepository userRepository;

    @InjectMocks
    private IngredientService ingredientService;

    @Test
    void shouldCreateIngredient() {

        CreateIngredientRequest request = createValidRequest();

        IngredientCategory category = createCategory();

        User user = createUser();

        when(userRepository.findByUsername("development-user"))
                .thenReturn(Optional.of(user));

        when(ingredientRepository.findByUserIdAndNameIgnoreCase(user.getId(), "Egg"))
                .thenReturn(Optional.empty());

        when(ingredientCategoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        Ingredient savedIngredient = createIngredient(category, user);

        when(ingredientRepository.save(any(Ingredient.class)))
                .thenReturn(savedIngredient);

        Ingredient ingredient = ingredientService.createIngredient(request);

        assertEquals(1L, ingredient.getId());
        assertEquals("Egg", ingredient.getName());
        assertEquals(Unit.EACH, ingredient.getDefaultUnit());
        assertEquals(category, ingredient.getIngredientCategory());
        assertNull(ingredient.getIngredientCategory().getUser());

        verify(ingredientRepository).findByUserIdAndNameIgnoreCase(user.getId(), "Egg");
        verify(ingredientCategoryRepository).findById(1L);

        ArgumentCaptor<Ingredient> ingredientCaptor = ArgumentCaptor.forClass(Ingredient.class);
        verify(ingredientRepository).save(ingredientCaptor.capture());
        Ingredient savedRequest = ingredientCaptor.getValue();
        assertEquals(user, savedRequest.getUser());

    }

    @Test
    void shouldThrowExceptionWhenIngredientAlreadyExists() {

        CreateIngredientRequest request = createValidRequest();

        User user = createUser();

        when(userRepository.findByUsername("development-user"))
                .thenReturn(Optional.of(user));

        Ingredient existingIngredient = Ingredient.builder()
                .id(1L)
                .name("Egg")
                .defaultUnit(Unit.EACH)
                .build();

        when(ingredientRepository.findByUserIdAndNameIgnoreCase(user.getId(), "Egg"))
                .thenReturn(Optional.of(existingIngredient));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ingredientService.createIngredient(request)
        );

        assertEquals(
                "An ingredient with the name 'Egg' already exists.",
                exception.getMessage()
        );

        verify(ingredientRepository).findByUserIdAndNameIgnoreCase(user.getId(), "Egg");
        verifyNoInteractions(ingredientCategoryRepository);
        verify(ingredientRepository, never()).save(any());

    }

    @Test
    void shouldThrowExceptionWhenIngredientCategoryDoesNotExist() {

        CreateIngredientRequest request = createValidRequest();

        User user = createUser();

        when(userRepository.findByUsername("development-user"))
                .thenReturn(Optional.of(user));

        when(ingredientRepository.findByUserIdAndNameIgnoreCase(user.getId(), "Egg"))
                .thenReturn(Optional.empty());

        when(ingredientCategoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ingredientService.createIngredient(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Ingredient category not found: 1", exception.getReason());

        verify(ingredientRepository).findByUserIdAndNameIgnoreCase(user.getId(), "Egg");
        verify(ingredientCategoryRepository).findById(1L);
        verify(ingredientRepository, never()).save(any());

    }

    @Test
    void shouldGetIngredient() {

        IngredientCategory category = createCategory();

        User user = createUser();

        when(userRepository.findByUsername("development-user"))
                .thenReturn(Optional.of(user));

        Ingredient ingredient = createIngredient(category, user);

        when(ingredientRepository.findByIdAndUserId(1L, user.getId()))
                .thenReturn(Optional.of(ingredient));

        IngredientResponse response = ingredientService.getIngredient(1L);

        assertEquals(1L, response.getId());
        assertEquals("Egg", response.getName());
        assertEquals(Unit.EACH, response.getDefaultUnit());
        assertEquals(category, response.getIngredientCategory());

        verify(ingredientRepository).findByIdAndUserId(1L, user.getId());

    }

    @Test
    void shouldThrowExceptionWhenIngredientDoesNotExist() {

        User user = createUser();

        when(userRepository.findByUsername("development-user"))
                .thenReturn(Optional.of(user));

        when(ingredientRepository.findByIdAndUserId(99L, user.getId()))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ingredientService.getIngredient(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Ingredient not found: 99", exception.getReason());

        verify(ingredientRepository).findByIdAndUserId(99L, user.getId());

    }

    @Test
    void shouldReturnAllIngredientsSortedByName() {
        User user = createUser();

        when(userRepository.findByUsername("development-user"))
                .thenReturn(Optional.of(user));

        Ingredient egg = Ingredient.builder()
                .id(1L)
                .name("Egg")
                .user(user)
                .build();

        Ingredient butter = Ingredient.builder()
                .id(2L)
                .name("Butter")
                .user(user)
                .build();

        when(ingredientRepository.findAllByUserIdOrderByNameAsc(user.getId()))
                .thenReturn(List.of(butter, egg));

        List<Ingredient> ingredients = ingredientService.getAllIngredients();

        assertEquals(2, ingredients.size());
        assertEquals("Butter", ingredients.get(0).getName());
        assertEquals("Egg", ingredients.get(1).getName());

        verify(ingredientRepository)
                .findAllByUserIdOrderByNameAsc(user.getId());

    }

    @Test
    void shouldFindIngredientsByName() {

        User user = createUser();

        when(userRepository.findByUsername("development-user"))
                .thenReturn(Optional.of(user));

        Ingredient egg = Ingredient.builder()
                .id(1L)
                .name("Egg")
                .build();

        Ingredient eggplant = Ingredient.builder()
                .id(2L)
                .name("Eggplant")
                .build();

        when(ingredientRepository.findTop20ByUserIdAndNameContainingIgnoreCase(user.getId(), "egg"))
                .thenReturn(List.of(egg, eggplant));

        List<Ingredient> ingredients = ingredientService.findIngredients("egg");

        assertEquals(2, ingredients.size());
        assertEquals("Egg", ingredients.get(0).getName());
        assertEquals("Eggplant", ingredients.get(1).getName());

        verify(ingredientRepository)
                .findTop20ByUserIdAndNameContainingIgnoreCase(user.getId(), "egg");

    }

    @Test
    void shouldNotReturnIngredientWhenItDoesNotBelongToCurrentUser() {

        User user = createUser();

        when(userRepository.findByUsername("development-user"))
                .thenReturn(Optional.of(user));

        when(ingredientRepository.findByIdAndUserId(1L, user.getId()))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ingredientService.getIngredient(1L)
        );

        assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode()),
                () -> assertEquals("Ingredient not found: 1", exception.getReason())
        );

    }

    @Test
    void shouldThrowExceptionWhenDevelopmentUserDoesNotExist() {

        CreateIngredientRequest request = createValidRequest();

        when(userRepository.findByUsername("development-user"))
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ingredientService.createIngredient(request)
        );

        assertEquals("Development user not found", exception.getMessage());

        verifyNoInteractions(ingredientRepository, ingredientCategoryRepository);

    }

    @Test
    void shouldRejectIngredientCategoryOwnedByAnotherUser() {

        CreateIngredientRequest request = createValidRequest();
        User currentUser = createUser();
        User anotherUser = User.builder()
                .id(2L)
                .username("another-user")
                .build();

        IngredientCategory category = IngredientCategory.builder()
                .id(1L)
                .name("Baking")
                .user(anotherUser)
                .build();

        when(userRepository.findByUsername("development-user"))
                .thenReturn(Optional.of(currentUser));
        when(ingredientRepository.findByUserIdAndNameIgnoreCase(currentUser.getId(), "Egg"))
                .thenReturn(Optional.empty());
        when(ingredientCategoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ingredientService.createIngredient(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Ingredient category not found: 1", exception.getReason());

        verify(ingredientRepository, never()).save(any(Ingredient.class));

    }

    @Test
    void shouldReturnStandardAndCurrentUsersIngredientCategories() {

        User user = createUser();

        IngredientCategory standardCategory= IngredientCategory.builder()
                .id(1L)
                .name("Baking")
                .build();

        IngredientCategory customCategory = IngredientCategory.builder()
                .id(2L)
                .name("Meal Prep")
                .user(user)
                .build();

        when(userRepository.findByUsername("development-user"))
                .thenReturn(Optional.of(user));

        when(ingredientCategoryRepository
                .findByUserIdOrUserIsNullOrderByNameAsc(user.getId()))
                .thenReturn(List.of(standardCategory, customCategory));

        List<IngredientCategory> categories = ingredientService.getAvailableIngredientCategories();

        assertEquals(2, categories.size());
        assertEquals("Baking", categories.get(0).getName());
        assertEquals("Meal Prep", categories.get(1).getName());

        assertNull(categories.get(0).getUser());
        assertEquals(user, categories.get(1).getUser());

        verify(ingredientCategoryRepository).findByUserIdOrUserIsNullOrderByNameAsc(user.getId());

    }

    private CreateIngredientRequest createValidRequest() {

        CreateIngredientRequest request = new CreateIngredientRequest();

        request.setName("Egg");
        request.setDefaultUnit(Unit.EACH);
        request.setIngredientCategoryId(1L);

        return request;

    }

    private IngredientCategory createCategory(){

        return IngredientCategory.builder()
                .id(1L)
                .name("Dairy")
                .build();

    }

    private Ingredient createIngredient(IngredientCategory category, User user) {

        return Ingredient.builder()
                .id(1L)
                .name("Egg")
                .defaultUnit(Unit.EACH)
                .ingredientCategory(category)
                .user(user)
                .build();

    }

    private User createUser() {

        return User.builder()
                .id(1L)
                .username("development-user")
                .build();

    }

}
