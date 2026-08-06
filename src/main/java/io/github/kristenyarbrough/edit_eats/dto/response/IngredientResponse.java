package io.github.kristenyarbrough.edit_eats.dto.response;

import io.github.kristenyarbrough.edit_eats.domain.IngredientCategory;
import io.github.kristenyarbrough.edit_eats.domain.Unit;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class IngredientResponse {

    private Long id;
    private String name;
    private Unit defaultUnit;
    private IngredientCategory ingredientCategory;

}
