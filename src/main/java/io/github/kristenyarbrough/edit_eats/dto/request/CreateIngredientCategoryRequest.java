package io.github.kristenyarbrough.edit_eats.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateIngredientCategoryRequest {

    @NotBlank(message = "Category name is required.")
    private String name;

}
