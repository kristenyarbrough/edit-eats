package io.github.kristenyarbrough.edit_eats.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IngredientCategoryResponse {

    private Long id;
    private String name;
    private boolean custom;

}
