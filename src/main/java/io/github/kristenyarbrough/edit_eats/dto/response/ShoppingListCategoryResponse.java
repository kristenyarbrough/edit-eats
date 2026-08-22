package io.github.kristenyarbrough.edit_eats.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ShoppingListCategoryResponse {

    private Long categoryId;
    private String categoryName;
    private List<ShoppingListItemResponse> items;

}
