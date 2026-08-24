package io.github.kristenyarbrough.edit_eats.dto.response;

import io.github.kristenyarbrough.edit_eats.domain.Difficulty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecipeSummaryResponse {

    private Long id;
    private String name;
    private Integer prepMinutes;
    private Integer cookMinutes;
    private Integer totalMinutes;
    private String formattedTotalTime;
    private Integer servings;
    private Difficulty difficulty;
    private String imageUrl;
    private List<RecipeCategoryResponse> categories;

}
