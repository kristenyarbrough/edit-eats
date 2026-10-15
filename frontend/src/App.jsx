import { useState } from 'react'
import './App.css'
import RecipeReview from './components/RecipeReview'

function App() {
    const [showImport, setShowImport] = useState(false)
    const [recipeText, setRecipeText] = useState('')
    const [recipe, setRecipe] = useState(null)
    const [saveMessage, setSaveMessage] = useState('')
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

        console.log('Recipe import response:', JSON.stringify(data, null, 2))

        setRecipe(data)

        } catch (error) {
            console.error(`Recipe import failed: `, error)
        }
    }
    const handleSave = async (recipeToSave) => {
        setSaveMessage('')

        try {
            console.log('Recipe save request:', JSON.stringify(recipeToSave, null, 2))

            const response = await fetch('/api/recipes', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(recipeToSave),
            })

            if (!response.ok) {
                const errorText = await response.text()
                throw new Error(
                    errorText || `Save failed (${response.status})`
                )
            }

            const data = await response.json()

            console.log('Recipe saved:', data)
            setSaveMessage(`Recipe "${data.name}" saved successfully.`)
            setRecipe(null)
            setRecipeText('')
            setShowImport(false)

        } catch (error) {
            console.error('Recipe save failed:', error)
            setSaveMessage(`Failed to save recipe: ${error.message}`)
        }
    }

    return (

        <div className="app">
            <header className="header">
                <img
                    src="/edit-eats-logo.svg"
                    alt="Edit Eats"
                    className="logo"
                />
{/*                 <h1>Edit Eats</h1> */}
                <p>Your recipes, organised your way.</p>
            </header>

            <main className="home">
                {!showImport ? (
                    <>
                        <div className="home-actions">
                            <button
                                className="home-action-card"
                                onClick={() => setShowImport(true)}
                            >
    {/*                             <icon name="plus" /> */}
                                <span className="home-action-icon" aria-hidden="true">+</span>
                                <span>Import Recipe</span>
                            </button>

                            <button className="home-action-card">
    {/*                             <icon name="book-open" /> */}
                                <span className="home-action-icon" aria-hidden="true">▤</span>
                                <span>My Recipes</span>
                            </button>
                        </div>
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

                        <div className="import-recipe-actions">
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
                    <RecipeReview
                        recipe={recipe}
                        onBack={() => setRecipe(null)}
                        onSave={handleSave}
                    />
                )}
                {saveMessage && (
                    <div className="save-notification" role="status">
                        <span className="save-notification-icon" aria-hidden="true">
                            ✓
                        </span>
                        <p>{saveMessage}</p>
                        <button
                            className="save-notification-close"
                            onClick={() => setSaveMessage('')}
                            aria-label="Dismiss notification"
                        >
                            ×
                        </button>
                    </div>
                )}
            </main>
        </div>

    )
}

export default App
