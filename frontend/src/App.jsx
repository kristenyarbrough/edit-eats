import { useState } from 'react'
import './App.css'
import RecipeReview from './components/RecipeReview'
import RecipeView from './components/RecipeView'
import RecipeCardImage from './components/RecipeCardImage'

function App() {
    const [showImport, setShowImport] = useState(false)
    const [recipeText, setRecipeText] = useState('')
    const [recipe, setRecipe] = useState(null)
    const [saveMessage, setSaveMessage] = useState('')
    const [savedRecipe, setSavedRecipe] = useState(null)
    const [showMyRecipes, setShowMyRecipes] = useState(false)
    const [myRecipes, setMyRecipes] = useState([])
    const [myRecipesError, setMyRecipesError] = useState('')
    const [returnToMyRecipes, setReturnToMyRecipes] = useState(false)
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

            const savedResponse = await fetch(`/api/recipes/${data.id}`)

            if (!savedResponse.ok) {
                throw new Error(`Recipe saved, but failed to load it (${savedResponse.status})`)
            }

            const savedData = await savedResponse.json()
            console.log('Saved recipe loaded:', savedData)

            setReturnToMyRecipes(true)
            setSavedRecipe(savedData)
            console.log('savedRecipe set to:', savedData)
            setSaveMessage(`Recipe "${data.name}" saved successfully.`)
            setRecipe(null)
            setRecipeText('')
            setShowImport(false)
            console.log('handleSave completed')

        } catch (error) {
            console.error('Recipe save failed:', error)
            setSaveMessage(`Failed to save recipe: ${error.message}`)
        }
    }
    const handleShowMyRecipes = async () => {
        try {
            const response = await fetch('/api/recipes')

            if (!response.ok) {
                throw new Error(`Failed to load recipes (${response.status})`)
            }

            const data = await response.json()
            console.log('Recipes loaded:', data)

            setMyRecipes(data)
            setShowMyRecipes(true)
        } catch (error) {
            console.error('Error loading recipes:', error)
            setMyRecipesError(error.message)
            setShowMyRecipes(true)
        }
    }
    const handleDeleteRecipe = async (recipeId, recipeName) => {
        const confirmed = window.confirm(
            `Are you sure you want to delete "${recipeName}"?`
        )

        if (!confirmed) {
            return
        }

        try {
            const response = await fetch(`/api/recipes/${recipeId}`, {
                method: 'DELETE'
            })

            if (!response.ok) {
                throw new Error('Failed to delete recipe')
            }

            setMyRecipes((currentRecipes) =>
                currentRecipes.filter((item) => item.id !== recipeId)
            )
        } catch (error) {
            console.error('Error deleting recipe:', error)
            setMyRecipesError(`Couldn't delete "${recipeName}". Please try again.`)
        }
    }

    return (

        <div className="app">
            {savedRecipe ? (
                <header className="app-recipe-header">
                    <img
                        src="/edit-eats-logo.svg"
                        alt="Edit Eats"
                        className="logo"
                    />
    {/*                 <h1>Edit Eats</h1> */}
                    <p>Your recipes, organised your way.</p>
                </header>
            ) : (
                <header className="header">
                    <img
                        src="/edit-eats-logo.svg"
                        alt="Edit Eats"
                        className="logo"
                    />
                    <p>Your recipes, organised your way.</p>
                </header>
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
            <main className="home">

                {savedRecipe ? (
                    <RecipeView
                        recipe={savedRecipe}
                        onBack={() => {
                            setSavedRecipe(null)
                            setShowMyRecipes(returnToMyRecipes)
                            setReturnToMyRecipes(false)}}
                    />
                ) : showMyRecipes ? (
                    <div className="my-recipes-section">
                        <h2>My Recipes</h2>

                        {myRecipesError && (
                            <p role="alert">{myRecipesError}</p>
                        )}

                        <button
                            className="back-button"
                            onClick={() => setShowMyRecipes(false)}
                        >
                            ← Back
                        </button>

                        {myRecipes.length === 0 ? (
                            <p>You don't have any recipes yet.</p>
                        ) : (
                            <div className="recipe-list">
                                {myRecipes.map((item) => (
                                    <div
                                        key={item.id}
                                        className="recipe-list-card"
                                    >
                                        <button
                                            type="button"
                                            className="recipe-card-open"
                                            onClick={async () => {
                                                try {
                                                    const response = await fetch(
                                                        `/api/recipes/${item.id}`
                                                    )

                                                    if (!response.ok) {
                                                        throw new Error('Failed to load recipe')
                                                    }

                                                    const data = await response.json()
                                                    setReturnToMyRecipes(true)
                                                    setSavedRecipe(data)
                                                    setShowMyRecipes(false)
                                                } catch (error) {
                                                    console.error('Error loading recipe:', error)
                                                }
                                            }}
                                        >
                                            <RecipeCardImage
                                                imageUrl={item.imageUrl}
                                                recipeName={item.name}
                                            />
                                            <div className="recipe-card-content">
                                                <h3>{item.name}</h3>

                                                <div className="recipe-card-details">
                                                    {item.formattedTotalTime && (
                                                        <span>⏱ {item.formattedTotalTime}</span>
                                                    )}

                                                    {item.servings != null && (
                                                        <span>Serves {item.servings}</span>
                                                    )}

                                                    {item.difficulty && (
                                                        <span>{item.difficulty}</span>
                                                    )}
                                                </div>

                                                {item.categories?.length > 0 && (
                                                    <div className="recipe-card-categories">
                                                        {item.categories.map((category) => (
                                                            <span key={category.id} className="recipe-category">
                                                                {category.name}
                                                            </span>
                                                        ))}
                                                    </div>
                                                )}
                                            </div>
                                        </button>
                                        <button
                                            type="button"
                                            className="delete-recipe-button"
                                            onClick={(event) => handleDeleteRecipe(item.id, item.name)}
                                        >
                                            Delete
                                        </button>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                ) : recipe ? (
                    <RecipeReview
                        recipe={recipe}
                        onBack={() => setRecipe(null)}
                        onSave={handleSave}
                    />
                ) : !showImport ? (
                    <div className="home-actions">
                        <button
                            className="home-action-card"
                            onClick={() => setShowImport(true)}
                        >
    {/*                         <icon name="plus" /> */}
                            <span className="home-action-icon" aria-hidden="true">+</span>
                            <span>Import Recipe</span>
                        </button>

                        <button
                            className="home-action-card"
                            onClick={handleShowMyRecipes}
                        >
    {/*                         <icon name="book-open" /> */}
                            <span className="home-action-icon" aria-hidden="true">▤</span>
                            <span>My Recipes</span>
                        </button>
                    </div>
                ) : (
                    <div className="import-recipe-section">
                        <h2>Import Recipe</h2>

                        <div className="textarea-wrapper">
                            <textarea
                                placeholder="Paste your recipe here..."
                                rows="15"
                                value={recipeText}
                                onChange={(event) => setRecipeText(event.target.value)}
                            />
                        </div>

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
            </main>
        </div>

    )
}

export default App
