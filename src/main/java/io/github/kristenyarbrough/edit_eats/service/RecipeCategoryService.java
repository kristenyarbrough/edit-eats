package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.RecipeCategory;
import io.github.kristenyarbrough.edit_eats.domain.User;
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

    public List<RecipeCategory> getAvailableCategories() {

        User user = getDevelopmentUser();

        return recipeCategoryRepository.findAllByUserIdIsNullOrUserIdOrderByNameAsc(user.getId());

    }

    public RecipeCategory createCategory(String name) {

        User user = getDevelopmentUser();

        recipeCategoryRepository
                .findByUserIdAndNameIgnoreCase(user.getId(), name)
                .ifPresent(category -> {
                    throw new IllegalArgumentException(
                            "A recipe category with the name '" + name + "' already exists."
                    );
                });

        RecipeCategory category = RecipeCategory.builder()
                .name(name)
                .user(user)
                .build();

        return recipeCategoryRepository.save(category);

    }

    private User getDevelopmentUser() {

        return userRepository.findByUsername("development-user")
                .orElseThrow(() -> new IllegalStateException(
                        "Development user not found"
                ));

    }

}
