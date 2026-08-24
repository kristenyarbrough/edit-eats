package io.github.kristenyarbrough.edit_eats.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ImportedRecipeResponse {

    private String name;
    private Integer prepMinutes;
    private Integer cookMinutes;
    private Integer servings;
    private String imageUrl;
    private String sourceUrl;

    private List<String> ingredients;
    private List<String> steps;

}
