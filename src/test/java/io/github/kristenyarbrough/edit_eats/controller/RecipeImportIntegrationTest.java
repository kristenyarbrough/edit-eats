package io.github.kristenyarbrough.edit_eats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.kristenyarbrough.edit_eats.domain.Ingredient;
import io.github.kristenyarbrough.edit_eats.domain.IngredientCategory;
import io.github.kristenyarbrough.edit_eats.domain.Unit;
import io.github.kristenyarbrough.edit_eats.domain.User;
import io.github.kristenyarbrough.edit_eats.dto.imported.ImportedRecipe;
import io.github.kristenyarbrough.edit_eats.repository.IngredientCategoryRepository;
import io.github.kristenyarbrough.edit_eats.repository.IngredientRepository;
import io.github.kristenyarbrough.edit_eats.repository.UserRepository;
import io.github.kristenyarbrough.edit_eats.service.RecipePageFetcher;
import io.github.kristenyarbrough.edit_eats.service.RecipeStructuredDataParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class RecipeImportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecipeStructuredDataParser structuredDataParser;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IngredientCategoryRepository ingredientCategoryRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

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

        when(pageFetcher.fetch(TEST_URL))
                .thenReturn(document);

        Elements scripts =
                document.select("script[type=application/ld+json]");

        assertEquals(1, scripts.size());
        assertFalse(scripts.first().data().isBlank());

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
        assertEquals("chicken breast", result.getIngredients().get(0).getName());
        assertEquals(new BigDecimal("500"), result.getIngredients().get(0).getQuantity());
        assertEquals(Unit.G, result.getIngredients().get(0).getUnit());
        assertEquals("onion", result.getIngredients().get(1).getName());
        assertEquals(new BigDecimal("1"), result.getIngredients().get(1).getQuantity());
        assertNull(result.getIngredients().get(1).getUnit());
        assertEquals("curry powder", result.getIngredients().get(2).getName());
        assertEquals(new BigDecimal("2"), result.getIngredients().get(2).getQuantity());
        assertEquals(Unit.TSP, result.getIngredients().get(2).getUnit());

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

    @Test
    void shouldSaveAndRetrieveImportedRecipe() throws Exception {

        Map<String, Long> ingredientIds = setUpTestIngredients();

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
                            "text": "Add the chicken and the curry powder."
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

        when(pageFetcher.fetch(TEST_URL)).thenReturn(document);

        String importedRecipeJson = mockMvc.perform(
                post("/api/recipes/import/url")
                        .param("url", TEST_URL)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Chicken Curry"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        ObjectNode createRequest = (ObjectNode) objectMapper.readTree(importedRecipeJson);
        ArrayNode ingredients = (ArrayNode) createRequest.get("ingredients");

        for (int i = 0; i < ingredients.size(); i++) {

            ObjectNode ingredient = (ObjectNode) ingredients.get(i);
            String ingredientName = ingredient.get("ingredientName").asText();
            Long ingredientId = ingredientIds.get(ingredientName);

            assertNotNull(ingredientId, "No test ingredient found for: " + ingredientName);

            ingredient.put("ingredientId", ingredientId);

            ingredient.put("optional", false);

        }

        createRequest.put("difficulty", "MEDIUM");

        // Remove fields that aren't accepted by CreateRecipeRequest
        createRequest.remove("activeMinutes");
        createRequest.remove("totalMinutes");
        createRequest.remove("formattedTotalTime");
        createRequest.remove("formattedActiveTime");
        createRequest.remove("categories");

        createRequest.putArray("categories");
        createRequest.putArray("ingredientSections");
        createRequest.putArray("instructionSections");

        String savedRecipeJson = mockMvc.perform(
                post("/api/recipes")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequest))
        )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long recipeId = objectMapper.readTree(savedRecipeJson)
                .get("id")
                .asLong();

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/recipes/{id}", recipeId)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(recipeId))
                .andExpect(jsonPath("$.name").value("Chicken Curry"))
                .andExpect(jsonPath("$.prepMinutes").value(15))
                .andExpect(jsonPath("$.cookMinutes").value(30))
                .andExpect(jsonPath("$.servings").value(4))
                .andExpect(jsonPath("$.difficulty").value("MEDIUM"))
                .andExpect(jsonPath("$.sourceUrl").value(TEST_URL))
                .andExpect(jsonPath("$.imageUrl")
                        .value("https://example.com/chicken-curry.jpg"))
                .andExpect(jsonPath("$.ingredients.length()").value(3))
                .andExpect(jsonPath("$.ingredients[0].ingredientName")
                        .value("chicken breast"))
                .andExpect(jsonPath("$.ingredients[0].quantity").value(500))
                .andExpect(jsonPath("$.ingredients[0].unit").value("G"))
                .andExpect(jsonPath("$.ingredients[0].optional").value(false))
                .andExpect(jsonPath("$.ingredients[1].ingredientName")
                        .value("onion"))
                .andExpect(jsonPath("$.ingredients[1].quantity").value(1))
                .andExpect(jsonPath("$.ingredients[1].unit").doesNotExist())
                .andExpect(jsonPath("$.ingredients[1].optional").value(false))
                .andExpect(jsonPath("$.ingredients[2].ingredientName")
                        .value("curry powder"))
                .andExpect(jsonPath("$.ingredients[2].quantity").value(2))
                .andExpect(jsonPath("$.ingredients[2].unit").value("TSP"))
                .andExpect(jsonPath("$.ingredients[2].optional").value(false))
                .andExpect(jsonPath("$.steps.length()").value(3))
                .andExpect(jsonPath("$.steps[0].instruction")
                        .value("Cut the chicken into pieces."))
                .andExpect(jsonPath("$.steps[2].instruction")
                        .value("Add the chicken and the curry powder."));

    }

    private Map<String, Long> setUpTestIngredients() {

        User user = userRepository.findByUsername("development-user")
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .username("development-user")
                                .build()
        ));

        IngredientCategory category = ingredientCategoryRepository.save(
                IngredientCategory.builder()
                        .name("Test Ingredients")
                        .user(user)
                        .build()
        );

        Map<String, Long> ingredientIds = new HashMap<>();

        Ingredient chickenBreast = ingredientRepository.save(
                Ingredient.builder()
                        .name("chicken breast")
                        .defaultUnit(Unit.G)
                        .ingredientCategory(category)
                        .user(user)
                        .build()
        );
        ingredientIds.put("chicken breast", chickenBreast.getId());

        Ingredient onion = ingredientRepository.save(
                Ingredient.builder()
                        .name("onion")
                        .defaultUnit(Unit.EACH)
                        .ingredientCategory(category)
                        .user(user)
                        .build()
        );
        ingredientIds.put("onion", onion.getId());

        Ingredient curryPowder = ingredientRepository.save(
                Ingredient.builder()
                        .name("curry powder")
                        .defaultUnit(Unit.TSP)
                        .ingredientCategory(category)
                        .user(user)
                        .build()
        );
        ingredientIds.put("curry powder", curryPowder.getId());

        return ingredientIds;

    }

}
