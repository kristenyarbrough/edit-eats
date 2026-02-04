package io.github.kristenyarbrough.edit_eats.dto;

import lombok.Data;
import java.util.List;

@Data
public class ShoppingListRequest {
    private List<Long> recipeIds;
}
