    package io.github.kristenyarbrough.edit_eats.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.github.kristenyarbrough.edit_eats.dto.response.ImportedRecipeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class RecipeStructuredDataParser {

    private static final Pattern ISO_DURATION_PATTERN =
            Pattern.compile(
                    "^PT(?:(\\d+)H)?(?:(\\d+)M)?$",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern INTEGER_PATTERN =
            Pattern.compile("\\d+");

    private final ObjectMapper objectMapper;

    public ImportedRecipeResponse parse(String json) {

        try {

            JsonNode root = objectMapper.readTree(json);

            return ImportedRecipeResponse.builder()
                    .name(root.path("name").asText(null))
                    .prepMinutes(parseDuration(root.path("prepTime").asText(null)))
                    .cookMinutes(parseDuration(root.path("cookTime").asText(null)))
                    .servings(parseServings(root.path("recipeYield").asText(null)))
                    .imageUrl(parseImage(root.path("image")))
                    .sourceUrl(root.path("url").asText(null))
                    .ingredients(parseIngredients(root.path("recipeIngredient")))
                    .steps(parseSteps(root.path("recipeInstructions")))
                    .build();

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Unable to parse recipe structured data",
                    e
            );

        }

    }

    private Integer parseDuration(String value) {

        if (value == null || value.isBlank()) {

            return null;

        }

        Matcher matcher = ISO_DURATION_PATTERN.matcher(value);

        if (!matcher.matches()) {

            return null;

        }

        int hours = matcher.group(1) == null
                ? 0
                : Integer.parseInt(matcher.group(1));

        int minutes = matcher.group(2) == null
                ? 0
                : Integer.parseInt(matcher.group(2));

        return hours * 60 + minutes;

    }

    private Integer parseServings(String value) {

        if (value == null || value.isBlank()) {

            return null;

        }

        Matcher matcher = INTEGER_PATTERN.matcher(value);

        if (!matcher.find()) {

            return null;

        }

        return Integer.parseInt(matcher.group());

    }

    private String parseImage(JsonNode node) {

        if (node.isTextual()) {

            return node.asText();

        }

        if (node.isArray() && !node.isEmpty()) {

            JsonNode first = node.get(0);

            if (first.isTextual()) {

                return first.asText();

            }

        }

        return null;

    }

    private List<String> parseIngredients(JsonNode node) {

        List<String> ingredients = new ArrayList<>();

        if (!node.isArray()) {

            return ingredients;

        }

        for (JsonNode ingredient : node) {

            if (ingredient.isTextual()) {

                ingredients.add(ingredient.asText());

            }

        }

        return ingredients;

    }

    private List<String> parseSteps(JsonNode node) {

        List<String> steps = new ArrayList<>();

        if (node.isTextual()) {

            steps.add(node.asText());

            return steps;

        }

        if (!node.isArray()) {

            return steps;

        }

        for (JsonNode step : node) {

            if (step.isTextual()) {

                steps.add(step.asText());

            } else if (step.isObject()) {

                JsonNode text = step.get("text");

                if (text != null && text.isTextual()) {

                    steps.add(text.asText());

                }

            }

        }

        return steps;

    }

}