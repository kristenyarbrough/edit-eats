package io.github.kristenyarbrough.edit_eats.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecipeIngredientSectionResponse {

    private String name;
    private List<RecipeIngredientResponse> ingredients;
    private List<RecipeIngredientSectionResponse> sections;

}
