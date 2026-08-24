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
- [x] Update RecipeIngredient entity
- [x] Create RecipeCategory entity (supporting multiple categories per recipe)
- [x] Create their repositories
- [x] Extend CreateRecipeRequest to include ingredients and categories
- [x] Update RecipeService.createRecipe() to save:
  - [x] the recipe
  - [x] the categories
  - [x] all ingredients
  - [x] all steps
  - in a single transaction

## Sprint 3 - Add meal plan functionality

### Meal Planning

- [x] Design meal plan model
- [x] Create MealPlan entity
- [x] Create MealType enum
- [x] Create MealPlanEntry entity
- [x] CRUD endpoints
  - [x] DTOs
  - [x] Repository
  - [x] Service
  - [x] Controller
- [x] Tests

## Sprint 4 - Complete Meal Plan Workflow

### Update and Delete Meal Plans

- [x] UpdateMealPlanRequest
- [x] MealPlanService.updateMealPlan()
- [x] Tests for Update
- [x] PUT controller endpoint + tests
- [x] Delete MealPlan + its MealPlanRecipes
- [x] Tests for Delete
- [x] DELETE controller endpoint + tests

## Sprint 5 - Generate shopping lists

### Shopping Lists

- [x] Generate shopping list
- [x] Combine duplicate ingredients
- [x] Convert units
- [x] Group by ingredient category

## Next Sprint - Import recipes

### Recipe Importing

- [ ] Import recipe from URL
- [ ] Extract ingredients
- [ ] Extract method
- [ ] Download recipe image
- [ ] Save source URL

---

## Future Features

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
- [x] Recipe search
- [ ] Recipe pagination
- [ ] Recently viewed recipes
- [ ] Favourite recipes
- [ ] Recipe ratings
- [ ] Import recipe from URL
- [ ] Upload image to Recipe
- [ ] Update recipe
- [ ] Add createdBy to Recipe
- [ ] Allow adding ingredient by name (search by name)

---

## Plan

### Phase 1 — Core recipe management (where we are now)
- [x] ✅ Ingredient
- [x] ✅ IngredientCategory
- [x] 🚧 Recipe
- [x] 🚧 RecipeStep
- [x] 🚧 RecipeIngredient
- [x] ⏳ RecipeCategory

### Phase 2 — Editing
- [x] Update recipe
- [x] Add/remove ingredients
- [ ] Reorder steps
- [x] Delete recipe

### Phase 3 — The exciting features
- [ ] Recipe import from URL
- [x] Shopping list generation
- [x] Scale recipes (2 serves → 8 serves)
- [ ] Pantry
- [x] Meal planner

### Phase 4 — Make the existing recipe system excellent

- [ ] 🔎 Recipe search/filtering
  - [ ] Search
    - [ ] Get recipe by ID
    - [ ] Search recipes
    - [ ] List recipes
  - [ ] Filter
    - [ ] Filter by category
    - [ ] Filter by ingredient
  - [ ] Sort
- [ ] ⭐ Favourite recipes
- [ ] 📥 Recipe upload/import
- [ ] ✏️ Edit imported recipe
- [ ] 🥫 Pantry/inventory/fridge
- [ ] ☑️ Shopping-list check-off/editing
  - [ ] provide totalMinutes and derive cookMinutes or prepMinutes?
  - [ ] passiveTime - important if things need to be made in advance
- [ ] Better unit converter, converting between metric and US

###  Phase 5 — Make meal planning smarter
- [ ] 📋 Save/reuse meal plans (Meal Plan templates)
- [ ] 🔄 Repeat a previous week
- [ ] 🧮 Smarter serving adjustments
- [ ] 🛒 Pantry-aware shopping lists

### Phase 6 — Turn it into an app
- [ ] 👥 Users and authentication
- [ ] 📱 Frontend/mobile app

###  Phase 7 — Personal dining
- [ ] 🍽️ Restaurants
- [ ] 📍 Restaurant visits
- [ ] 🍴 Dishes ordered
- [ ] ⭐ Personal dish ratings
- [ ] 📝 Personal notes
- [ ] 📷 Food photos
- [ ] 👥 Number of diners
- [ ] 💰 Price / spend
- [ ] 🔁 "What did I order last time?"
- [ ] ❤️ Favourite dishes
- [ ] 🚫 Never-order-again dishes
- [ ] 💡 "What should I order next time?"

###  Phase 8 — Personal intelligence

"I'm going back to this restaurant tonight. What should I order?"

Edit Eats could look at your own history and answer from your own experiences.

## Roadmap
### Now

<h4 style="text-align: center;">🔎 Recipe search/filtering</h4>

<p style="text-align: center;">↓</p>

<h4 style="text-align: center;">🖥️ Build the first React UI</h4>

<p style="text-align: center;">↓</p>

<h4 style="text-align: center;">⭐ Favourites</h4>

<p style="text-align: center;">↓</p>

<h4 style="text-align: center;">🖥️ Favourites UI</h4>

<p style="text-align: center;">↓</p>

<h4 style="text-align: center;">📥 Recipe upload/import</h4>

<p style="text-align: center;">↓</p>

<h4 style="text-align: center;">🖥️ Recipe editor</h4>

<p style="text-align: center;">↓</p>

<h4 style="text-align: center;">🥫 Pantry</h4>

<p style="text-align: center;">↓</p>

<h4 style="text-align: center;">🛒 Smarter shopping list</h4>

<p style="text-align: center;">↓</p>

<h4 style="text-align: center;">📅 Reusable meal plans</h4>

<p style="text-align: center;">↓</p>

<h4 style="text-align: center;">🍽️ Dining Journal</h4>

<p style="text-align: center;">↓</p>

<h4 style="text-align: center;">📱 Eventually mobile</h4>