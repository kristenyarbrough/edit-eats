package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.Ingredient;
import io.github.kristenyarbrough.edit_eats.domain.IngredientCategory;
import io.github.kristenyarbrough.edit_eats.domain.User;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateIngredientRequest;
import io.github.kristenyarbrough.edit_eats.dto.response.IngredientResponse;
import io.github.kristenyarbrough.edit_eats.repository.IngredientCategoryRepository;
import io.github.kristenyarbrough.edit_eats.repository.IngredientRepository;
import io.github.kristenyarbrough.edit_eats.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientCategoryRepository ingredientCategoryRepository;
    private final UserRepository userRepository;

    public Ingredient createIngredient(CreateIngredientRequest request) {

        User user = getDevelopmentUser();

        ingredientRepository.findByUserIdAndNameIgnoreCase(user.getId(), request.getName())
                .ifPresent(ingredient -> {
                    throw new IllegalArgumentException(
                            "An ingredient with the name '" + request.getName() + "' already exists.");
        });

        IngredientCategory category =
                ingredientCategoryRepository
                        .findById(request.getIngredientCategoryId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Ingredient category not found: " + request.getIngredientCategoryId()));

        Ingredient ingredient = Ingredient.builder()
                .name(request.getName())
                .defaultUnit(request.getDefaultUnit())
                .ingredientCategory(category)
                .build();

        return ingredientRepository.save(ingredient);

    }

    public List<Ingredient> getAllIngredients() {

        User user = getDevelopmentUser();

        return ingredientRepository.findAllByUserIdOrderByNameAsc(user.getId());

    }

    public List<Ingredient> findIngredients(String name) {

        User user = getDevelopmentUser();

        return ingredientRepository.findTop20ByUserIdAndNameContainingIgnoreCase(
                user.getId(),
                name
        );

    }

    public IngredientResponse getIngredient(Long id) {

        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Ingredient not found: " + id));

        return IngredientResponse.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .defaultUnit(ingredient.getDefaultUnit())
                .ingredientCategory(ingredient.getIngredientCategory())
                .build();
    }

    private User getDevelopmentUser() {

        return userRepository.findByUsername("development-user")
                .orElseThrow(() -> new IllegalStateException(
                        "Development user not found"
                ));

    }

}
