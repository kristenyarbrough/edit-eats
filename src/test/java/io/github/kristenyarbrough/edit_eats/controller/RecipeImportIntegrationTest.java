package io.github.kristenyarbrough.edit_eats.controller;

import io.github.kristenyarbrough.edit_eats.dto.imported.ImportedRecipe;
import io.github.kristenyarbrough.edit_eats.service.RecipePageFetcher;
import io.github.kristenyarbrough.edit_eats.service.RecipeStructuredDataParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RecipeImportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecipeStructuredDataParser structuredDataParser;

    @MockitoBean
    private RecipePageFetcher pageFetcher;

    private static final String TEST_URL =
            "https://example.com/chicken-curry";

    @Test
    void shouldImportRecipeFromUrlThroughControllerAndService() throws Exception {

        String jsonLd = """
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
                        "2 tsp curry powder"
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
                            "text": "Add the chicken and curry powder."
                        }
                    ]
                }
                """;

        Document document= Jsoup.parse("""
                <html>
                    <head>
                        <script type="application/ld+json">
                        %s
                        </script>
                    </head>
                </html>
                """.formatted(jsonLd));

        System.out.println("TEST SCRIPT COUNT = "
                + document.select("script[type=application/ld+json]").size());

        System.out.println("TEST SCRIPT DATA = "
                + document.select("script[type=application/ld+json]").first().data());

        when(pageFetcher.fetch(TEST_URL))
                .thenReturn(document);

        Elements scripts =
                document.select("script[type=application/ld+json]");

        assertEquals(1, scripts.size());
        assertFalse(scripts.first().data().isBlank());

        System.out.println("JSON-LD:");
        System.out.println(scripts.first().data());
        System.out.println("SCRIPT COUNT = "
                + document.select("script[type=application/ld+json]").size());

        System.out.println("SCRIPT DATA = "
                + document.select("script[type=application/ld+json]")
                .first()
                .data());

        when(pageFetcher.fetch("https://example.com/chicken-curry"))
                .thenReturn(document);

        ImportedRecipe parsed =
                structuredDataParser.parse(document
                        .select("script[type=application/ld+json]")
                        .first()
                        .data());

        assertEquals("Chicken Curry", parsed.getName());
        assertEquals(3, parsed.getIngredients().size());

        mockMvc.perform(
                post("/api/recipes/import/url")
                        .param("url", "https://example.com/chicken-curry")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Chicken Curry"))
                .andExpect(jsonPath("$.prepMinutes").value(15))
                .andExpect(jsonPath("$.cookMinutes").value(30))
                .andExpect(jsonPath("$.activeMinutes").value(45))
                .andExpect(jsonPath("$.passiveMinutes").value(0))
                .andExpect(jsonPath("$.totalMinutes").value(45))
                .andExpect(jsonPath("$.servings").value(4))
                .andExpect(jsonPath("$.sourceUrl")
                        .value("https://example.com/chicken-curry"))
                .andExpect(jsonPath("$.imageUrl")
                        .value("https://example.com/chicken-curry.jpg"))
                .andExpect(jsonPath("$.ingredients").isArray())
                .andExpect(jsonPath("$.ingredients.length()").value(3))
                .andExpect(jsonPath("$.ingredients[0].ingredientName")
                        .value("chicken breast"))
                .andExpect(jsonPath("$.ingredients[0].quantity").value(500))
                .andExpect(jsonPath("$.ingredients[0].unit").value("G"))
                .andExpect(jsonPath("$.ingredients[1].ingredientName")
                        .value("onion"))
                .andExpect(jsonPath("$.ingredients[1].quantity").value(1))
                .andExpect(jsonPath("$.ingredients[2].ingredientName")
                        .value("curry powder"))
                .andExpect(jsonPath("$.ingredients[2].quantity").value(2))
                .andExpect(jsonPath("$.ingredients[2].unit").value("TSP"))
                .andExpect(jsonPath("$.steps").isArray())
                .andExpect(jsonPath("$.steps.length()").value(3))
                .andExpect(jsonPath("$.steps[0].stepNumber").value(1))
                .andExpect(jsonPath("$.steps[0].instruction")
                        .value("Cut the chicken into pieces."))
                .andExpect(jsonPath("$.steps[1].stepNumber").value(2))
                .andExpect(jsonPath("$.steps[2].stepNumber").value(3))
                .andExpect(jsonPath("$.categories").isEmpty());

        verify(pageFetcher).fetch("https://example.com/chicken-curry");

    }

    @Test
    void shouldParseRecipeIngredients() {

        String json = """
            {
                "@context": "https://schema.org",
                "@type": "Recipe",
                "name": "Chicken Curry",
                "recipeIngredient": [
                    "500 g chicken breast",
                    "1 onion",
                    "2 tsp curry powder"
                ]
            }
            """;

        ImportedRecipe result = structuredDataParser.parse(json);

        assertEquals("Chicken Curry", result.getName());
        assertEquals(3, result.getIngredients().size());
        assertEquals("500 g chicken breast", result.getIngredients().get(0).getName());
        assertEquals("1 onion", result.getIngredients().get(1).getName());
        assertEquals("2 tsp curry powder", result.getIngredients().get(2).getName());

    }

    @Test
    void shouldImportRecipeFromGraphStructuredData() throws Exception {

        String jsonLd = """
                {
                    "@context": "https://schema.org",
                    "@graph": [
                        {
                            "@type": "WebSite",
                            "name": "Example Recipes"
                        },
                        {
                            "@type": "Recipe",
                            "name": "Chicken Curry",
                            "image": "https://example.com/chicken-curry.jpg",
                            "prepTime": "PT15M",
                            "cookTime": "PT30M",
                            "recipeYield": "4 servings",
                            "recipeIngredient": [
                                "500 g chicken breast",
                                "1 onion",
                                "2 tsp curry powder"
                            ],
                            "recipeInstructions": [
                                {
                                    "@type": "HowToStep",
                                    "text": "Cut the chicken into pieces."
                                },
                                {
                                    "@type": "HowToStep",
                                    "text": "Cook the onion until softened."
                                }
                            ]
                        }
                    ]
                }
                """;

        Document document = Jsoup.parse("""
                <html>
                    <head>
                        <script type="application/ld+json">
                            %s
                        </script>
                    </head>
                </html>
                """.formatted(jsonLd));

        when(pageFetcher.fetch("https://example.com/chicken-curry"))
                .thenReturn(document);

        mockMvc.perform(
                post("/api/recipes/import/url")
                        .param("url", "https://example.com/chicken-curry")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Chicken Curry"))
                .andExpect(jsonPath("$.ingredients.length()").value(3))
                .andExpect(jsonPath("$.steps.length()").value(2))
                .andExpect(jsonPath("$.prepMinutes").value(15))
                .andExpect(jsonPath("$.cookMinutes").value(30))
                .andExpect(jsonPath("$.activeMinutes").value(45))
                .andExpect(jsonPath("$.totalMinutes").value(45));

    }

    @Test
    void shouldImportRecipeJsonAmongMultipleStructuredDataScripts() throws Exception {

        String websiteJsonLd = """
                {
                    "@context": "https://schema.org",
                    "@type": "WebSite",
                    "name": "Example Recipes"
                }
                """;

        String breadcrumbJsonLd = """
                {
                    "@context": "https://schema.org",
                    "@type": "BreadcrumbList",
                    "itemListElement": []
                }
                """;

        String recipeJsonLd = """
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
                        "2 tsp curry powder"
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
                            "text": "Add the chicken and curry powder."
                        }
                    ]
                }
                """;

        Document document = Jsoup.parse("""
                <html>
                    <head>
                        <script type="application/ld+json">
                        %s
                        </script>
                        
                        <script type="application/ld+json">
                        %s
                        </script>
                        
                        <script type="application/ld+json">
                        %s
                        </script>
                    </head>
                </html>
                """.formatted(
                websiteJsonLd,
                breadcrumbJsonLd,
                recipeJsonLd
        ));

        when(pageFetcher.fetch(
                "https://example.com/chicken-curry"))
                .thenReturn(document);

        mockMvc.perform(
                post("/api/recipes/import/url")
                        .param(
                                "url",
                                "https://example.com/chicken-curry"
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Chicken Curry"))
                .andExpect(jsonPath("$.prepMinutes").value(15))
                .andExpect(jsonPath("$.cookMinutes").value(30))
                .andExpect(jsonPath("$.activeMinutes").value(45))
                .andExpect(jsonPath("$.passiveMinutes").value(0))
                .andExpect(jsonPath("$.totalMinutes").value(45))
                .andExpect(jsonPath("$.servings").value(4))
                .andExpect(jsonPath("$.ingredients.length()").value(3))
                .andExpect(jsonPath("$.ingredients[0].ingredientName")
                        .value("chicken breast"))
                .andExpect(jsonPath("$.ingredients[0].quantity")
                        .value(500))
                .andExpect(jsonPath("$.ingredients[0].unit")
                        .value("G"))
                .andExpect(jsonPath("$.ingredients[1].ingredientName")
                        .value("onion"))
                .andExpect(jsonPath("$.ingredients[1].quantity").value(1))
                .andExpect(jsonPath("$.ingredients[2].ingredientName")
                        .value("curry powder"))
                .andExpect(jsonPath("$.ingredients[2].unit").value("TSP"))
                .andExpect(jsonPath("$.steps.length()").value(3))
                .andExpect(jsonPath("$.steps[0].stepNumber").value(1))
                .andExpect(jsonPath("$.steps[1].stepNumber").value(2))
                .andExpect(jsonPath("$.steps[2].stepNumber").value(3))
                .andExpect(jsonPath("$.categories").isEmpty());

    }

}
