import { useState } from 'react'
import './App.css'

function IngredientSection({ section }) {
    return (
        <div>
            <h3>{section.name}</h3>

            {section.ingredients.map((ingredient, index) => (
                <p key={index}>
                    {ingredient.quantity && `${ingredient.quantity} `}
                    {ingredient.unit && `${ingredient.unit.toLowerCase()} `}
                    {ingredient.ingredientName}
                    {ingredient.preparation && `, ${ingredient.preparation}`}
                    {ingredient.optional && ' (optional)'}
                </p>
            ))}

            {section.sections.map((nestedSection, index) => (
                <IngredientSection
                    key={index}
                    section={nestedSection}
                />
            ))}
        </div>
    )
}
function InstructionSection({ section }) {
    return (
        <div>
            <h3>{section.name}</h3>

            {section.steps.map((step, index) => (
                <p key={index}>
                    {step.stepNumber}. {step.instruction}
                </p>
            ))}

            {section.sections.map((nestedSection, index) => (
                <InstructionSection
                    key={index}
                    section={nestedSection}
                />
            ))}
        </div>
    )
}
function App() {
    const [showImport, setShowImport] = useState(false)
    const [recipeText, setRecipeText] = useState('')
    const [recipe, setRecipe] = useState(null)
    const handleImport = async () => {
        try {
            const response = await fetch(
                `/api/recipes/import/text?text=${encodeURIComponent(recipeText)}`,
                {
                    method: 'POST',
                }
            )

            if (!response.ok) {
                const errorText = await response.text()
                throw new Error(errorText || `Import failed (${response.status})`)
            }

        const data = await response.json()

        setRecipe(data)

        } catch (error) {
            console.error(`Recipe import failed: `, error)
        }
    }

    return (

        <div className="app">
            <header className="header">
                <h1>Edit Eats</h1>
                <p>Your recipes, organised your way.</p>
            </header>

            <main className="home">
                {!showImport ? (
                    <>
                        <button onClick={() => setShowImport(true)}>
                            Import Recipe
                        </button>

                        <button>
                            My Recipes
                        </button>
                    </>
                ) : (
                    <div>
                        <h2>Import Recipe</h2>

                        <textarea
                            placeholder="Paste your recipe here..."
                            rows="15"
                            value={recipeText}
                            onChange={(event) => setRecipeText(event.target.value)}
                        />

                        <div>
                            <button onClick={() => setShowImport(false)}>
                                Cancel
                            </button>

                            <button onClick={handleImport}>
                                Import
                            </button>
                        </div>
                    </div>
                )}
                {recipe && (
                    <div>
                        <h2>{recipe.name}</h2>

                        <p>Serves: {recipe.servings}</p>

                        <p>Prep: {recipe.prepMinutes} minutes</p>

                        <p>Cook: {recipe.cookMinutes} minutes</p>

                        <h2>Ingredients</h2>

                        {recipe.ingredients.map((ingredient, index) => (
                            <p key={index}>
                                {ingredient.quantity && `${ingredient.quantity} `}
                                {ingredient.unit && `${ingredient.unit.toLowerCase()} `}
                                {ingredient.ingredientName}
                                {ingredient.preparation && `, ${ingredient.preparation}`}
                                {ingredient.optional && ' (optional)'}
                            </p>
                        ))}

                        {recipe.ingredientSections.map((section, index) => (
                            <IngredientSection
                                key={index}
                                section={section}
                            />
                        ))}

                        <h2>Method</h2>
                        {recipe.steps.map((step, index) => (
                            <p key={index}>
                                {step.stepNumber}. {step.instruction}
                            </p>
                        ))}

                        {recipe.instructionSections.map((section, index) => (
                            <InstructionSection
                                key={index}
                                section={section}
                            />
                        ))}

                    </div>
                )}
            </main>
        </div>

    )
}

export default App
