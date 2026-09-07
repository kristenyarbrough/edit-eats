package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.Unit;
import io.github.kristenyarbrough.edit_eats.dto.imported.ImportedIngredient;
import io.github.kristenyarbrough.edit_eats.dto.imported.ImportedInstructionSection;
import io.github.kristenyarbrough.edit_eats.dto.imported.ImportedRecipe;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RecipeStructuredDataParserTest {

    private final RecipeStructuredDataParser parser =
            new RecipeStructuredDataParser(new tools.jackson.databind.json.JsonMapper());

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

        ImportedRecipe result = parser.parse(json);

        assertEquals("Chicken Curry", result.getName());
        assertEquals(15, result.getPrepMinutes());
        assertEquals(30, result.getCookMinutes());
        assertEquals(45, result.getTotalMinutes());
        assertEquals(4, result.getServings());
        assertEquals("https://example.com/chicken-curry.jpg", result.getImageUrl());

        assertEquals(3, result.getIngredients().size());
        assertEquals("500 g chicken breast", result.getIngredients().get(0).getName());
        assertEquals("1 onion", result.getIngredients().get(1).getName());
        assertEquals("400 ml coconut milk", result.getIngredients().get(2).getName());

        assertEquals(3, result.getSteps().size());
        assertEquals("Cut the chicken into pieces.", result.getSteps().get(0).getInstruction());
        assertEquals("Cook the onion until softened.", result.getSteps().get(1).getInstruction());
        assertEquals("Add the chicken and coconut milk.", result.getSteps().get(2).getInstruction());

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

        ImportedRecipe result = parser.parse(json);

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

        ImportedRecipe result = parser.parse(json);

        assertEquals(1, result.getSteps().size());
        assertEquals(
                "Mix the ingredients. Bake for 30 minutes.",
                result.getSteps().get(0).getInstruction()
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

        ImportedRecipe result = parser.parse(json);

        assertEquals(
                "https://example.com/chicken-curry.jpg",
                result.getImageUrl()
        );

    }

    @Test
    void shouldFindRecipeInsideGraph() {

        String json = """
                {
                    "@context": "https://schema.org",
                    "@graph": [
                        {
                            "@type": "WebSite",
                            "name": "Example Recipes"
                        },
                        {
                            "@type": "BreadcrumbList",
                            "itemListElement": []
                        },
                        {
                            "@type": "Recipe",
                            "name": "Chicken Curry",
                            "prepTime": "PT15M",
                            "cookTime": "PT30M",
                            "recipeYield": "4 servings",
                            "recipeIngredient": [
                                "500 g chicken breast",
                                "1 onion"
                            ]
                        }
                    ]
                }
                """;

        ImportedRecipe result = parser.parse(json);

        assertEquals("Chicken Curry", result.getName());
        assertEquals(15, result.getPrepMinutes());
        assertEquals(30, result.getCookMinutes());
        assertEquals(45, result.getTotalMinutes());
        assertEquals(4, result.getServings());
        assertEquals(2, result.getIngredients().size());

    }

    @Test
    void shouldFindRecipeWhenTypeIsArray() {

        String json = """
                {
                    "@context": "https://schema.org",
                    "@type": ["Recipe", "NewsArticle"],
                    "name": "Chicken Curry",
                    "prepTime": "PT15M",
                    "cookTime": "PT30M",
                    "recipeIngredient": [
                        "500 g chicken breast"
                    ]
                }
                """;

        ImportedRecipe result = parser.parse(json);

        assertEquals("Chicken Curry", result.getName());
        assertEquals(15, result.getPrepMinutes());
        assertEquals(30, result.getCookMinutes());

    }

    @Test
    void shouldRejectStructuredDataWithoutRecipe() {

        String json = """
                {
                    "@context": "https://schema.org",
                    "@type": "WebSite",
                    "name": "Example Recipes"
                }
                """;

        IllegalArgumentException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> parser.parse(json)
                );

        assertEquals("Unable to parse recipe structured data",
                exception.getMessage());

    }

    @Test
    void shouldUseSuppliedTotalTime() {

        String json = """
                {
                    "@type": "Recipe",
                    "name": "Slow Cooked Beef",
                    "prepTime": "PT15M",
                    "cookTime": "PT2H",
                    "totalTime": "PT3H30M"
                }
                """;

        ImportedRecipe result = parser.parse(json);

        assertEquals(15, result.getPrepMinutes());
        assertEquals(120, result.getCookMinutes());
        assertEquals(210, result.getTotalMinutes());

    }

    @Test
    void shouldParseHowToSectionInstructions() {

        String json = """
                {
                    "@type": "Recipe",
                    "name": "Chicken Curry",
                    "recipeInstructions":[
                        {
                            "@type": "HowToSection",
                            "name": "Prepare the chicken",
                            "itemListElement": [
                                {
                                    "@type": "HowToStep",
                                    "text": "Cut the chicken into pieces."
                                },
                                {
                                    "@type": "HowToStep",
                                    "text": "Season the chicken."
                                }
                            ]
                        },
                        {
                            "@type": "HowToStep",
                            "text": "Cook the chicken until golden."
                        }
                    ]
                }
                """;

        ImportedRecipe result = parser.parse(json);

        assertEquals(3, result.getSteps().size());
        assertEquals("Cut the chicken into pieces.",
                result.getSteps().get(0).getInstruction());
        assertEquals("Season the chicken.",
                result.getSteps().get(1).getInstruction());
        assertEquals("Cook the chicken until golden.",
                result.getSteps().get(2).getInstruction());
        assertEquals(1, result.getSteps().get(0).getStepNumber());
        assertEquals(2, result.getSteps().get(1).getStepNumber());
        assertEquals(3, result.getSteps().get(2).getStepNumber());

    }

    @Test
    void shouldParseNestedHowToSection() {

        String json = """
                {
                    "@type": "Recipe",
                    "name": "Chicken Curry",
                    "recipeInstructions": [
                        {
                            "@type": "HowToSection",
                            "name": "Prepare",
                            "itemListElement": [
                                {
                                    "@type": "HowToSection",
                                    "name": "Chicken",
                                    "itemListElement": [
                                        {
                                            "@type": "HowToStep",
                                            "text": "Cut the chicken into pieces."
                                        },
                                        {
                                            "@type": "HowToStep",
                                            "text": "Season the chicken."
                                        }
                                    ]
                                },
                                {
                                    "@type": "HowToSection",
                                    "name": "Sauce",
                                    "itemListElement": [
                                        {
                                            "@type": "HowToStep",
                                            "text": "Heat the pan."
                                        },
                                        {
                                            "@type": "HowToStep",
                                            "text": "Add the sauce."
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                }
                """;

        ImportedRecipe result = parser.parse(json);

        assertEquals(4, result.getSteps().size());
        assertEquals("Cut the chicken into pieces.", result.getSteps().get(0).getInstruction());
        assertEquals("Season the chicken.", result.getSteps().get(1).getInstruction());
        assertEquals("Heat the pan.", result.getSteps().get(2).getInstruction());
        assertEquals("Add the sauce.", result.getSteps().get(3).getInstruction());

    }

    @Test
    void shouldPreserveHowToSectionName() {

        String json = """
                {
                    "@type": "Recipe",
                    "name": "Chicken Pasta",
                    "recipeInstructions": [
                        {
                            "@type": "HowToSection",
                            "name": "Prepare the Chicken",
                            "itemListElement": [
                                {
                                    "@type": "HowToStep",
                                    "text": "Cut the chicken into pieces."
                                },
                                {
                                    "@type": "HowToStep",
                                    "text": "Cook the chicken until golden."
                                }
                            ]
                        }
                    ]
                }
                """;

        ImportedRecipe result = parser.parse(json);

        assertNotNull(result);
        assertNotNull(result.getInstructionSections());
        assertEquals(1, result.getInstructionSections().size());

        ImportedInstructionSection section = result.getInstructionSections().get(0);

        assertEquals("Prepare the Chicken", section.getName());

        assertNotNull(section.getSteps());
        assertEquals(2, section.getSteps().size());
        assertEquals("Cut the chicken into pieces.", section.getSteps().get(0).getInstruction());
        assertEquals("Cook the chicken until golden.", section.getSteps().get(1).getInstruction());

    }

    @Test
    void shouldParseInstructionSections() {

        String json = """
                {
                    "@type": "Recipe",
                    "name": "Chicken Curry",
                    "recipeInstructions": [
                        {
                            "@type": "HowToSection",
                            "name": "Prepare the chicken",
                            "itemListElement": [
                                {
                                    "@type": "HowToStep",
                                    "text": "Cut the chicken into pieces."
                                },
                                {
                                    "@type": "HowToStep",
                                    "text": "Season the chicken."
                                }
                            ]
                        },
                        {
                            "@type": "HowToSection",
                            "name": "Make the sauce",
                            "itemListElement": [
                                {
                                    "@type": "HowToStep",
                                    "text": "Heat the pan."
                                },
                                {
                                    "@type": "HowToStep",
                                    "text": "Add the sauce."
                                }
                            ]
                        }
                    ]
                }
                """;

        ImportedRecipe result = parser.parse(json);

        assertEquals(2, result.getInstructionSections().size());

        ImportedInstructionSection prepareSection = result.getInstructionSections().get(0);

        assertEquals("Prepare the chicken", prepareSection.getName());
        assertEquals(2, prepareSection.getSteps().size());
        assertEquals("Cut the chicken into pieces.", prepareSection.getSteps().get(0).getInstruction());
        assertEquals("Season the chicken.", prepareSection.getSteps().get(1).getInstruction());

        ImportedInstructionSection sauceSection = result.getInstructionSections().get(1);

        assertEquals("Make the sauce", sauceSection.getName());
        assertEquals(2, sauceSection.getSteps().size());
        assertEquals("Heat the pan.", sauceSection.getSteps().get(0).getInstruction());
        assertEquals("Add the sauce.", sauceSection.getSteps().get(1).getInstruction());

    }

    @Test
    void shouldPreserveNestedHowToSections() {

        String json = """
                {
                    "@type": "Recipe",
                    "name": "Chicken Curry",
                    "recipeInstructions": [
                        {
                            "@type": "HowToSection",
                            "name": "Prepare",
                            "itemListElement": [
                                {
                                    "@type": "HowToSection",
                                    "name": "Chicken",
                                    "itemListElement": [
                                        {
                                            "@type": "HowToStep",
                                            "text": "Cut the chicken into pieces."
                                        },
                                        {
                                            "@type": "HowToStep",
                                            "text": "Season the chicken."
                                        }
                                    ]
                                },
                                {
                                    "@type": "HowToSection",
                                    "name": "Sauce",
                                    "itemListElement": [
                                        {
                                            "@type": "HowToStep",
                                            "text": "Heat the pan."
                                        },
                                        {
                                            "@type": "HowToStep",
                                            "text": "Add the sauce."
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                }
                """;

        ImportedRecipe result = parser.parse(json);

        assertNotNull(result);
        assertNotNull(result.getInstructionSections());
        assertEquals(1, result.getInstructionSections().size());

        ImportedInstructionSection prepare = result.getInstructionSections().get(0);

        assertEquals("Prepare", prepare.getName());
        assertNotNull(prepare.getSections());
        assertEquals(2, prepare.getSections().size());

        ImportedInstructionSection chicken = prepare.getSections().get(0);

        assertEquals("Chicken", chicken.getName());
        assertEquals(2, chicken.getSteps().size());
        assertEquals("Cut the chicken into pieces.", chicken.getSteps().get(0).getInstruction());
        assertEquals("Season the chicken.", chicken.getSteps().get(1).getInstruction());

        ImportedInstructionSection sauce = prepare.getSections().get(1);
        assertEquals(2, sauce.getSteps().size());
        assertEquals("Heat the pan.", sauce.getSteps().get(0).getInstruction());
        assertEquals("Add the sauce.", sauce.getSteps().get(1).getInstruction());

    }

    @Test
    void shouldPreserveStepsAndNestedSectionsInSameSection() {

        String json = """
                {
                    "@type": "Recipe",
                    "name": "Chicken Curry",
                    "recipeInstructions": [
                        {
                            "@type": "HowToSection",
                            "name": "Prepare the curry",
                            "itemListElement": [
                                {
                                    "@type": "HowToStep",
                                    "text": "Heat the oil."
                                },
                                {
                                    "@type": "HowToSection",
                                    "name": "Prepare the chicken",
                                    "itemListElement": [
                                        {
                                            "@type": "HowToStep",
                                            "text": "Cut the chicken into pieces." 
                                        },
                                        {
                                            "@type": "HowToStep",
                                            "text": "Season the chicken."
                                        }
                                    ]
                                },
                                {
                                    "@type": "HowToStep",
                                    "text": "Add the chicken to the pan."
                                }
                            ]
                        }
                    ]
                }
                """;

        ImportedRecipe result = parser.parse(json);

        assertEquals(1, result.getInstructionSections().size());

        ImportedInstructionSection section = result.getInstructionSections().get(0);

        assertEquals("Prepare the curry", section.getName());
        assertEquals(2, section.getSteps().size());
        assertEquals("Heat the oil.", section.getSteps().get(0).getInstruction());
        assertEquals("Add the chicken to the pan.",
                section.getSteps().get(1).getInstruction());
        assertEquals(1, section.getSections().size());

        ImportedInstructionSection chickenSection = section.getSections().get(0);

        assertEquals("Prepare the chicken", chickenSection.getName());
        assertEquals(2, chickenSection.getSteps().size());
        assertEquals("Cut the chicken into pieces.",
                chickenSection.getSteps().get(0).getInstruction());
        assertEquals("Season the chicken.",
                chickenSection.getSteps().get(1).getInstruction());

    }

    @Test
    void shouldParseFlatInstructionSteps() {

        String json = """
                {
                    "@type": "Recipe",
                    "name": "Chicken Curry",
                    "recipeInstructions": [
                        {
                            "@type": "HowToStep",
                            "text": "Cut the chicken into pieces."
                        },
                        {
                            "@type": "HowToStep",
                            "text": "Season the chicken."
                        },
                        {
                            "@type": "HowToStep",
                            "text": "Heat the pan."
                        }
                    ]
                }
                """;

        ImportedRecipe result = parser.parse(json);

        assertNotNull(result);
        assertNotNull(result.getSteps());
        assertEquals(3, result.getSteps().size());
        assertEquals("Cut the chicken into pieces.",
                result.getSteps().get(0).getInstruction());
        assertEquals("Season the chicken.",
                result.getSteps().get(1).getInstruction());
        assertEquals("Heat the pan.",
                result.getSteps().get(2).getInstruction());

    }

    @Test
    void shouldParseTextIngredients() {

        String json = """
                {
                    "@type": "Recipe",
                    "name": "Chicken Curry",
                    "recipeIngredient": [
                        "500g chicken breast",
                        "1 onion",
                        "2 cloves garlic",
                        "1 tablespoon curry powder"
                    ]
                }
                """;

        ImportedRecipe result = parser.parse(json);

        assertNotNull(result);
        assertNotNull(result.getIngredients());
        assertEquals(4, result.getIngredients().size());
        assertEquals("500g chicken breast", result.getIngredients().get(0).getName());
        assertEquals("1 onion", result.getIngredients().get(1).getName());
        assertEquals("2 cloves garlic", result.getIngredients().get(2).getName());
        assertEquals("1 tablespoon curry powder", result.getIngredients().get(3).getName());

    }

    @Test
    void shouldParseStructuredIngredients() {

        String json = """
                {
                    "@type": "Recipe",
                    "name": "Chicken Curry",
                    "recipeIngredient": [
                        {
                            "@type": "PropertyValue",
                            "value": "500",
                            "name": "chicken"
                        },
                        {
                            "@type": "PropertyValue",
                            "value": "2",
                            "name": "onion"
                        }
                    ]
                }
                """;

        ImportedRecipe result = parser.parse(json);

        assertNotNull(result);
        assertNotNull(result.getIngredients());
        assertEquals(2, result.getIngredients().size());
        assertEquals("chicken", result.getIngredients().get(0).getName());
        assertEquals("onion", result.getIngredients().get(1).getName());

    }

    @Test
    void shouldParseMixedTextAndStructuredIngredients() {

        String json  = """
                {
                    "@type": "Recipe",
                    "name": "Chicken Curry",
                    "recipeIngredient": [
                        "500g chicken breast",
                        {
                            "@type": "PropertyValue",
                            "name": "coconut milk",
                            "value": "400ml"
                        },
                        "2 cloves garlic"
                    ]
                }
                """;

        ImportedRecipe result = parser.parse(json);

        assertNotNull(result);
        assertNotNull(result.getIngredients());
        assertEquals(3, result.getIngredients().size());
        assertEquals("500g chicken breast", result.getIngredients().get(0).getName());
        assertEquals("coconut milk", result.getIngredients().get(1).getName());
        assertEquals("2 cloves garlic", result.getIngredients().get(2).getName());

    }

    @Test
    void shouldParseStructuredIngredientFields() {

        String json = """
                {
                    "@type": "Recipe",
                    "name": "Chicken Curry",
                    "recipeIngredient": [
                        {
                            "@type": "PropertyValue",
                            "name": "chicken",
                            "value": "500",
                            "unitCode": "GRM"
                        }
                    ]
                }
                """;

        ImportedRecipe result = parser.parse(json);

        assertEquals(1, result.getIngredients().size());

        ImportedIngredient ingredient = result.getIngredients().get(0);
        assertEquals("chicken", ingredient.getName());
        assertEquals(new BigDecimal("500"), ingredient.getQuantity());
        assertEquals(Unit.G, ingredient.getUnit());

    }
}