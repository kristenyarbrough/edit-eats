package io.github.kristenyarbrough.edit_eats.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "recipe_category_assignment",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_recipe_category_assignment",
                columnNames = {
                        "recipe_id",
                        "recipe_category_id"
                }
    ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeCategoryAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipe_category_id")
    private RecipeCategory recipeCategory;

}
