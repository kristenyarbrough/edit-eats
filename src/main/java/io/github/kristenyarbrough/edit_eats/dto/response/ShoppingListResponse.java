package io.github.kristenyarbrough.edit_eats.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ShoppingListResponse {

    private List<ShoppingListCategoryResponse> categories;

}
