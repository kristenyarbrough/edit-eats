package io.github.kristenyarbrough.edit_eats.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RecipeImportRequest {

    @NotBlank
    private String url;

}
