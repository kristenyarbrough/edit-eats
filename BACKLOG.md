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
- [ ] Create RecipeIngredient entity
- [x] Create RecipeRepository
- [x] Create RecipeService
- [x] Create RecipeController

### Testing
- [ ] Test in Swagger

---

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