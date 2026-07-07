package io.github.kristenyarbrough.edit_eats.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecipeCategoryResponse {

    private Long id;
    private String name;

}
