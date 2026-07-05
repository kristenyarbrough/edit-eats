# Edit Eats Backlog

## Sprint 1 - Recipe Management

### Ingredient Categories
- [x] Create IngredientCategory entity
- [x] Create IngredientCategoryRepository
- [x] Create IngredientCategoryService
- [x] Create IngredientCategoryController

### Ingredients
- [x] Create Ingredient entity
- [x] Create IngredientRepository
- [x] Create CreateIngredientRequest DTO
- [x] Create IngredientService
- [x] Create IngredientController

### Recipes
- [x] Create Recipe entity
- [x] Create RecipeIngredient entity
- [x] Create RecipeRepository
- [x] Create RecipeService
- [x] Create RecipeController
- [x] Create RecipeStep entity
- [x] Create RecipeStepRepository

### Testing
- [x] Test in Swagger

---

## Sprint 2 - Complete Recipe Creation Workflow

### Recipes
- [ ] Update RecipeIngredient entity
- [ ] Create RecipeCategory entity (supporting multiple categories per recipe)
- [ ] Create their repositories
- [ ] Extend CreateRecipeRequest to include ingredients and categories
- [ ] Update RecipeService.createRecipe() to save:
  - [ ] the recipe
  - [ ] the categories
  - [ ] all ingredients
  - [ ] all steps
  - in a single transaction

## Next Sprint

### Meal Planning

- [ ] Design meal plan model
- [ ] Create MealPlan entity
- [ ] Create MealPlanEntry entity
- [ ] CRUD endpoints

---

## Future Features

### Recipe Importing

- [ ] Import recipe from URL
- [ ] Extract ingredients
- [ ] Extract method
- [ ] Download recipe image
- [ ] Save source URL

### Shopping Lists

- [ ] Generate shopping list
- [ ] Combine duplicate ingredients
- [ ] Convert units
- [ ] Group by ingredient category

### Pantry

- [ ] Track pantry items
- [ ] Suggest recipes from pantry
- [ ] Highlight missing ingredients

### Users

- [ ] Registration
- [ ] Login
- [ ] JWT authentication
- [ ] Personal ingredient categories
- [ ] Personal recipe categories

---

## Nice to Have

- [ ] Favourite recipes
- [ ] Recipe ratings
- [ ] Notes on recipes
- [ ] Nutrition information
- [ ] Dark mode
- [ ] Update README.md


# Domain Model

## Recipe
- id
- name
- prepMinutes
- cookMinutes
- servings
- difficulty
- sourceUrl
- imageUrl
- storageInstructions
- freezerInstructions

Relationships:
- RecipeIngredients
- RecipeSteps
- RecipeCategories

## RecipeIngredient
- recipe
- ingredient
- quantity
- unit
- preparation
- optional
- displayOrder

## RecipeStep
- recipe
- stepNumber
- instruction

## Ingredient
- name
- defaultUnit
- ingredientCategory

## RecipeCategory
- name

## IngredientCategory
- name

# Future Improvements

- [x] Add createdAt to Recipe
- [x] Add lastModifiedAt to Recipe
- [ ] Recipe search
- [ ] Recipe pagination
- [ ] Recently viewed recipes
- [ ] Favourite recipes
- [ ] Recipe ratings
- [ ] Import recipe from URL
- [ ] Upload image to Recipe
- [ ] Update recipe
- [ ] Add createdBy to Recipe

---

## Plan

### Phase 1 — Core recipe management (where we are now)
- [x] ✅ Ingredient
- [x] ✅ IngredientCategory
- [ ] 🚧 Recipe
- [ ] 🚧 RecipeStep
- [ ] 🚧 RecipeIngredient
- [ ] ⏳ RecipeCategory

### Phase 2 — Retrieval
- [ ] Get recipe by ID
- [ ] Search recipes
- [ ] List recipes
- [ ] Filter by category
- [ ] Filter by ingredient
- [ ] Sort

### Phase 3 — Editing
- [ ] Update recipe
- [ ] Add/remove ingredients
- [ ] Reorder steps
- [ ] Delete recipe

### Phase 4 — The exciting features
- [ ] Recipe import from URL
- [ ] Shopping list generation
- [ ] Scale recipes (2 serves → 8 serves)
- [ ] Pantry
- [ ] Meal planner