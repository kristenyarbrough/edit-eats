package io.github.kristenyarbrough.edit_eats.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recipe")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer prepMinutes;

    @Column(nullable = false)
    private Integer cookMinutes;

    @Column(nullable = false)
    private Integer servings;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    private String sourceUrl;
    private String imageUrl;

    @Lob
    private String storageInstructions;

    @Lob
    private String freezerInstructions;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastModifiedAt;

    @Transient
    public int getTotalMinutes() {
        return (prepMinutes == null ? 0 : prepMinutes)
                + (cookMinutes == null ? 0 : cookMinutes);
    }

    @Transient
    public String getFormattedTotalTime() {
        int total = getTotalMinutes();

        int hours = total / 60;
        int minutes = total % 60;

        if (hours == 0) {
            return minutes + " mins";
        }

        if (minutes == 0) {
            return hours + (hours == 1 ? " hr" : " hrs");
        }

        return hours + (hours == 1 ? " hr " : " hrs ") + minutes + " mins";
    }
}
//private OffsetDateTime createdAt;
//
//@OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
//@Builder.Default
//private List<RecipeIngredient> ingredients = new ArrayList<>();