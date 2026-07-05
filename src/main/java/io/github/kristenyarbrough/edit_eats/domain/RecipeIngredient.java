package io.github.kristenyarbrough.edit_eats.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(
        name = "recipe_ingredient",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_recipe_ingredient",
                columnNames = {"recipe_id", "ingredient_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit;

    private String preparation;

    @Column(nullable = false)
    @Builder.Default
    private Boolean optional = false;
}
