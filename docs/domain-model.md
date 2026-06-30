# Domain Model

## Vision

Edit Eats is a personal recipe manager.

Users can:
- Create their own recipes
- Import recipes from websites
- Modify imported recipes
- Plan meals
- Generate shopping lists

## Core Entities

### Recipe

Represents a recipe owned by a user.

Fields:
- id
- name
- instructions
- prepTime
- cookTime
- difficulty
- servings
- sourceUrl
- imageUrl
- storageInstructions
- freezerInstructions

### Ingredient

Represents a reusable ingredient.

Fields:
- id
- name
- defaultUnit
- ingredientCategory

### IngredientCategory

Represents a user-defined category for ingredients.

Examples:
- Vegetables
- Dairy
- Meat
- Pantry
- Baking

Fields:
- id
- name

### RecipeIngredient

Connects a recipe to an ingredient.

Fields:
- recipe
- ingredient
- quantity
- unit
- preparation
- optional