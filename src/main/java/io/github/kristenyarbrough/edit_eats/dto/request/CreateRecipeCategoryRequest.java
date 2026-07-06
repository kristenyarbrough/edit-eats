package io.github.kristenyarbrough.edit_eats.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRecipeCategoryRequest {

    @NotNull
    private Long recipeCategoryId;

}
