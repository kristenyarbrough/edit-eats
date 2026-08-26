package io.github.kristenyarbrough.edit_eats.service;

import tools.jackson.databind.ObjectMapper;
import io.github.kristenyarbrough.edit_eats.dto.response.ImportedRecipeResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecipeStructuredDataParserTest {

    private final RecipeStructuredDataParser parser =
            new RecipeStructuredDataParser(new ObjectMapper());
    @Test
    void shouldParseRecipeFromSchemaOrgJsonLd() {

        String json = """
                {
                    "@context": "https://schema.org",
                    "@type": "Recipe",
                    "name": "Chicken Curry",
                    "image": "https://example.com/chicken-curry.jpg",
                    "prepTime": "PT15M",
                    "cookTime": "PT30M",
                    "recipeYield": "4 servings",
                    "recipeIngredient": [
                        "500 g chicken breast",
                        "1 onion",
                        "400 ml coconut milk"
                    ],
                    "recipeInstructions": [
                        {
                            "@type": "HowToStep",
                            "text": "Cut the chicken into pieces."
                        },
                        {
                            "@type": "HowToStep",
                            "text": "Cook the onion until softened."
                        },
                        {
                            "@type": "HowToStep",
                            "text": "Add the chicken and coconut milk."
                        }
                    ]
                }
                """;

        ImportedRecipeResponse result = parser.parse(json);

        assertEquals("Chicken Curry", result.getName());
        assertEquals(15, result.getPrepMinutes());
        assertEquals(30, result.getCookMinutes());
        assertEquals(4, result.getServings());
        assertEquals("https://example.com/chicken-curry.jpg", result.getImageUrl());

        assertEquals(3, result.getIngredients().size());
        assertEquals("500 g chicken breast", result.getIngredients().get(0));
        assertEquals("1 onion", result.getIngredients().get(1));
        assertEquals("400 ml coconut milk", result.getIngredients().get(2));

        assertEquals(3, result.getSteps().size());
        assertEquals("Cut the chicken into pieces.", result.getSteps().get(0));
        assertEquals("Cook the onion until softened.", result.getSteps().get(1));
        assertEquals("Add the chicken and coconut milk.", result.getSteps().get(2));

    }

    @Test
    void shouldParseHourAndMinuteDurations() {

        String json = """
            {
                "@type": "Recipe",
                "name": "Slow Cooked Beef",
                "prepTime": "PT1H15M",
                "cookTime": "PT2H30M"
            }
            """;

        ImportedRecipeResponse result = parser.parse(json);

        assertEquals(75, result.getPrepMinutes());
        assertEquals(150, result.getCookMinutes());

    }

    @Test
    void shouldParseTextRecipeInstructions() {

        String json = """
            {
                "@type": "Recipe",
                "name": "Simple Cake",
                "recipeInstructions": "Mix the ingredients. Bake for 30 minutes."
            }
            """;

        ImportedRecipeResponse result = parser.parse(json);

        assertEquals(1, result.getSteps().size());
        assertEquals(
                "Mix the ingredients. Bake for 30 minutes.",
                result.getSteps().get(0)
        );

    }

    @Test
    void shouldParseImageArray() {

        String json = """
            {
                "@type": "Recipe",
                "name": "Chicken Curry",
                "image": [
                    "https://example.com/chicken-curry.jpg"
                ]
            }
            """;

        ImportedRecipeResponse result = parser.parse(json);

        assertEquals(
                "https://example.com/chicken-curry.jpg",
                result.getImageUrl()
        );

    }

}
