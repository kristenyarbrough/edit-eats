package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.dto.imported.ImportedIngredient;
import io.github.kristenyarbrough.edit_eats.dto.imported.ImportedRecipe;
import io.github.kristenyarbrough.edit_eats.dto.imported.ImportedStep;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class RecipeStructuredDataParser {

    private final JsonMapper jsonMapper;

    public ImportedRecipe parse(String json) {

        try {

            JsonNode root = jsonMapper.readTree(json);

            Integer prepMinutes =
                    parseDuration(root.path("prepTime").asText(null));

            Integer cookMinutes =
                    parseDuration(root.path("cookTime").asText(null));

            Integer totalMinutes =
                    parseDuration(root.path("totalTime").asText(null));

            if (totalMinutes == null
                    && prepMinutes != null
                    && cookMinutes != null) {

                totalMinutes = prepMinutes + cookMinutes;

            }

            return ImportedRecipe.builder()
                    .name(root.path("name").asText(null))
                    .prepMinutes(prepMinutes)
                    .cookMinutes(cookMinutes)
                    .totalMinutes(totalMinutes)
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

        Matcher matcher = Pattern.compile(
                "^PT(?:(\\d+)H)?(?:(\\d+)M)?$",
                Pattern.CASE_INSENSITIVE
        ).matcher(value);

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

        Matcher matcher = Pattern.compile("\\d+").matcher(value);

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

    private List<ImportedIngredient> parseIngredients(JsonNode node) {

        List<ImportedIngredient> ingredients = new ArrayList<>();

        if (!node.isArray()) {

            return ingredients;

        }

        for (JsonNode ingredient : node) {

            if (ingredient.isTextual()) {

                ingredients.add(
                        ImportedIngredient.builder()
                                .name(ingredient.asText())
                                .build()
                );

            } else if (ingredient.isObject()) {

                String name = ingredient.path("name").asText(null);

                if (name != null) {

                    ingredients.add(
                            ImportedIngredient.builder()
                                    .name(name)
                                    .build()
                    );

                }

            }

        }

        return ingredients;

    }

    private List<ImportedStep> parseSteps(JsonNode node) {

        List<ImportedStep> steps = new ArrayList<>();

        if (node.isTextual()) {

            steps.add(
                    ImportedStep.builder()
                            .stepNumber(1)
                            .instruction(node.asText())
                            .build()
            );

            return steps;

        }

        if (!node.isArray()) {

            return steps;

        }

        for (JsonNode step : node) {

            String instruction = null;

            if (step.isTextual()) {

                instruction = step.asText();

            } else if (step.isObject()) {

                JsonNode text = step.get("text");

                if (text != null && text.isTextual()) {

                    instruction = text.asText();

                }

            }

            if (instruction != null) {

                steps.add(
                        ImportedStep.builder()
                                .stepNumber(steps.size() + 1)
                                .instruction(instruction)
                                .build()
                );

            }

        }

        return steps;

    }

}