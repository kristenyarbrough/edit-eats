package io.github.kristenyarbrough.edit_eats.mealplan;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // We store the Monday (or chosen start date) for the plan
    @Column(nullable = false)
    private LocalDate weekStarting;

    @ElementCollection
    @CollectionTable(name = "meal_plan_recipe_ids", joinColumns = @JoinColumn(name = "meal_plan_id"))
    @Column(name = "recipe_id", nullable = false)
    @Builder.Default
    private List<Long> recipeIds = new ArrayList<>();
}
