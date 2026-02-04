# Edit Eats 🍲🗓️
A Spring Boot + Java 17 backend for storing recipes, planning meals, and generating a combined shopping list with unit conversions.

This started as a personal project to build real-world Java backend experience (service layer, JPA relationships, transactions, request validation, and API design) and to create something genuinely useful.

---

## What it does
- **Recipes**
    - Create, update, delete recipes
    - Store method, servings, prep/cook times, optional URLs, storage/freezer notes
    - Store ingredients with quantity + unit
    - Search recipes by name (case-insensitive)
    - Pagination on list + search

- **Meal plans**
    - Create/replace a meal plan for a given week
    - Generate a **shopping list from a meal plan** (aggregates ingredients across recipes)

- **Unit conversions**
    - g ↔ kg
    - ml ↔ l
    - tsp ↔ tbsp
    - cup ↔ ml
    - Shopping list totals are converted + summed correctly

---

## Tech stack
- Java 17
- Spring Boot
- Spring Web (REST)
- Spring Data JPA (Hibernate)
- H2 Database (file-based persistence)
- Lombok
- Springdoc OpenAPI (Swagger UI)

---

## Run locally
### Prerequisites
- Java 17 installed
- Maven (or use Maven wrapper if included)

### Start the app
```bash
mvn spring-boot:run
