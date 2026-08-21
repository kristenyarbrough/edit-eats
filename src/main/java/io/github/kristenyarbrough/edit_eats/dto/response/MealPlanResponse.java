package io.github.kristenyarbrough.edit_eats.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MealPlanResponse {

    private Long id;
    private String name;
    private LocalDate startDate;
    private Integer durationDays;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private List<MealPlanRecipeResponse> meals;

}
