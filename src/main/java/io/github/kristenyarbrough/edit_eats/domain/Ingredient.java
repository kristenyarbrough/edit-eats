package io.github.kristenyarbrough.edit_eats.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ingredient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit defaultUnit;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ingredient_category_id")
    private IngredientCategory ingredientCategory;

}
