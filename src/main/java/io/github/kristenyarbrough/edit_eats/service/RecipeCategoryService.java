package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.RecipeCategory;
import io.github.kristenyarbrough.edit_eats.domain.User;
import io.github.kristenyarbrough.edit_eats.dto.response.RecipeCategoryResponse;
import io.github.kristenyarbrough.edit_eats.repository.RecipeCategoryRepository;
import io.github.kristenyarbrough.edit_eats.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeCategoryService {

    private final RecipeCategoryRepository recipeCategoryRepository;
    private final UserRepository userRepository;

    public List<RecipeCategoryResponse> getAvailableCategories() {

        User user = getDevelopmentUser();

        return recipeCategoryRepository
                .findAllByUserIdIsNullOrUserIdOrderByNameAsc(user.getId())
                .stream()
                .map(category -> RecipeCategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .toList();

    }

    public RecipeCategoryResponse createCategory(String name) {

        User user = getDevelopmentUser();

        if (recipeCategoryRepository.existsAvailableCategoryForUser(
                user.getId(),
                name
        )) {
            throw new IllegalArgumentException(
                    "A recipe category with the name '" + name + "' already exists."
            );
        }

        RecipeCategory category = RecipeCategory.builder()
                .name(name)
                .user(user)
                .build();

        RecipeCategory savedCategory = recipeCategoryRepository.save(category);

        return RecipeCategoryResponse.builder()
                .id(savedCategory.getId())
                .name(savedCategory.getName())
                .build();

    }

    private User getDevelopmentUser() {

        return userRepository.findByUsername("development-user")
                .orElseThrow(() -> new IllegalStateException(
                        "Development user not found"
                ));

    }

}
