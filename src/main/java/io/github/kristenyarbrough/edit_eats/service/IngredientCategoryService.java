package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.IngredientCategory;
import io.github.kristenyarbrough.edit_eats.domain.User;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateIngredientCategoryRequest;
import io.github.kristenyarbrough.edit_eats.dto.response.IngredientCategoryResponse;
import io.github.kristenyarbrough.edit_eats.repository.IngredientCategoryRepository;
import io.github.kristenyarbrough.edit_eats.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngredientCategoryService {

    private final IngredientCategoryRepository ingredientCategoryRepository;
    private final UserRepository userRepository;

    public IngredientCategoryService(
            IngredientCategoryRepository ingredientCategoryRepository,
            UserRepository userRepository) {
        this.ingredientCategoryRepository = ingredientCategoryRepository;
        this.userRepository = userRepository;
    }

    public IngredientCategoryResponse createIngredientCategory(CreateIngredientCategoryRequest request) {

        User user = getDevelopmentUser();

        if (ingredientCategoryRepository.existsByNameAndUserId(request.getName(), user.getId())
                || ingredientCategoryRepository.existsByNameAndUserIdIsNull(request.getName())) {
            throw new IllegalArgumentException(
                    "Category '" + request.getName() + "' already exists.");
        }

        IngredientCategory category = IngredientCategory.builder()
                .name(request.getName())
                .user(user)
                .build();

        IngredientCategory savedCategory = ingredientCategoryRepository.save(category);

        return toResponse(savedCategory);

    }

    public List<IngredientCategoryResponse> getAllIngredientCategories() {

        User user = getDevelopmentUser();

        return ingredientCategoryRepository
                .findByUserIdOrUserIsNullOrderByNameAsc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();

    }

    private User getDevelopmentUser() {

        return userRepository.findByUsername("development-user")
                .orElseThrow(() -> new IllegalStateException(
                        "Development user not found"));

    }

    private IngredientCategoryResponse toResponse(IngredientCategory category) {

        return IngredientCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .custom(category.getUser() != null)
                .build();

    }

}
