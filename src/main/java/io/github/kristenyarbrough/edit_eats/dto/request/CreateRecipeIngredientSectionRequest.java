package io.github.kristenyarbrough.edit_eats.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateRecipeIngredientSectionRequest {

    @NotBlank
    private String name;

    @Valid
    private List<CreateRecipeIngredientRequest> ingredients = new ArrayList<>();

    @Valid
    private List<CreateRecipeIngredientSectionRequest> sections = new ArrayList<>();

}
