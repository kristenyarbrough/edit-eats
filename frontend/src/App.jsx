import { useState, useEffect, useRef } from 'react'
import { Plus, BookOpen, X, Import, MoveLeft, Trash, CheckCircle, CircleAlert, Pencil } from 'lucide-react'
import './App.css'
import RecipeReview from './components/RecipeReview'
import RecipeView from './components/RecipeView'
import RecipeCardImage from './components/RecipeCardImage'

function App() {
    const [showImport, setShowImport] = useState(false)
    const [importMode, setImportMode] = useState('text')
    const [recipeUrl, setRecipeUrl] = useState('')
    const [recipeText, setRecipeText] = useState('')
    const [recipe, setRecipe] = useState(null)
    const [saveMessage, setSaveMessage] = useState('')
    const [saveError, setSaveError] = useState('')
    const [savedRecipe, setSavedRecipe] = useState(null)
    const [isEditingRecipe, setIsEditingRecipe] = useState(false)
    const [showMyRecipes, setShowMyRecipes] = useState(false)
    const [myRecipes, setMyRecipes] = useState([])
    const [myRecipesError, setMyRecipesError] = useState('')
    const [returnToMyRecipes, setReturnToMyRecipes] = useState(false)
    const [deleteMessage, setDeleteMessage] = useState('')
    const [deleteError, setDeleteError] = useState('')
    const [recipeToDelete, setRecipeToDelete] = useState(null)
    const notificationRef = useRef(null)
    const deleteNotificationRef = useRef(null)
    useEffect(() => {
        console.log(
            'recipeToDelete:',
            JSON.stringify(recipeToDelete)
        )
    }, [recipeToDelete])
    useEffect(() => {
        if (saveMessage || saveError) {
            notificationRef.current?.scrollIntoView({
                behavior: 'smooth',
                block: 'center'
            })
        }
    }, [saveMessage, saveError])
    useEffect(() => {
        if (deleteMessage) {
            deleteNotificationRef.current?.scrollIntoView({
                behavior: 'smooth',
                block: 'center'
            })
        }
    }, [deleteMessage])
    useEffect(() => {
        if (savedRecipe) {
            window.scrollTo({
                top: 0,
                behavior: 'instant'
            })
        }
    }, [savedRecipe])
    const handleImport = async () => {
        try {
            const url = importMode === 'url'
                ? `/api/recipes/import/url?url=${encodeURIComponent(recipeUrl)}`
                : `/api/recipes/import/text?text=${encodeURIComponent(recipeText)}`

            const response = await fetch(url, {
                method: 'POST',
            })

            if (!response.ok) {
                const errorText = await response.text()
                throw new Error(errorText || `Import failed (${response.status})`)
            }

        const data = await response.json()

        console.log('Recipe import response:', JSON.stringify(data, null, 2))

        setRecipe(data)

        } catch (error) {
            console.error('Recipe import failed: ', error)
        }
    }
    const handleSave = async (recipeToSave) => {
        setSaveMessage('')
        setSaveError('')

        try {
            console.log('Recipe save request:', JSON.stringify(recipeToSave, null, 2))

//             throw new Error('Test save failure')

            const response = await fetch(
                isEditingRecipe
                    ? `/api/recipes/${recipeToSave.id}`
                    : '/api/recipes',
                {
                    method: isEditingRecipe? 'PUT' : 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(recipeToSave),
                }
            )

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

            setSaveMessage(savedData.name)
            setRecipe(null)
            setRecipeText('')
            setShowImport(false)
            setIsEditingRecipe(false)

            if (returnToMyRecipes) {
                setMyRecipes((currentRecipes) =>
                    currentRecipes.map((item) =>
                        item.id === savedData.id ? savedData : item
                    )
                )
                setShowMyRecipes(true)
                setReturnToMyRecipes(false)
            } else {
                setSavedRecipe(savedData)
            }

        } catch (error) {
            console.error('Recipe save failed:', error)
            setSaveError(`Failed to save recipe: ${error.message}`)
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
    const handleEditRecipe = async (recipeId) => {
        try {
            const response = await fetch(`/api/recipes/${recipeId}`)

            if (!response.ok) {
                throw new Error('Failed to load recipe')
            }

            const data = await response.json()
            setRecipe(data)
            setIsEditingRecipe(true)
            setReturnToMyRecipes(true)
            setShowMyRecipes(false)
        } catch (error) {
            console.error('Error loading recipe for editing:', error)
        }
    }
    const handleDeleteRecipe = (recipeId, recipeName) => {
        console.log('Opening delete dialog for:', recipeId, recipeName)

        setDeleteError('')
        setDeleteMessage('')
        setRecipeToDelete({ id: recipeId, name: recipeName})
    }
    const confirmDeleteRecipe = async () => {
//         const confirmed = window.confirm(
//             `Are you sure you want to delete "${recipeName}"?`
//         )

        if (!recipeToDelete) {
            return
        }

        const { id, name } = recipeToDelete

        try {
            const response = await fetch(`/api/recipes/${id}`, {
                method: 'DELETE'
            })
//
//             throw new Error('Test deletion failure')

            if (!response.ok) {
                const errorText = await response.text()

                throw new Error(
                    errorText || `Failed to delete recipe (${response.status})`
                )
            }

            setMyRecipes((currentRecipes) =>
                currentRecipes.filter((item) => item.id !== id)
            )

            setDeleteMessage(name)
            setDeleteError('')
            setMyRecipesError('')

            if(savedRecipe?.id === id) {
                setSavedRecipe(null)
                setShowMyRecipes(returnToMyRecipes)
                setReturnToMyRecipes(false)
            }
        } catch (error) {
            console.error('Error deleting recipe:', error)
            setDeleteError(name)
            setMyRecipesError(error.message)
        } finally {
            setRecipeToDelete(null)
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
                <div
                    ref={notificationRef}
                    className="save-notification"
                    role="status"
                >
                    <span className="save-notification-icon" aria-hidden="true">
                        <CheckCircle size={20} />
                    </span>
                    <p>
                        Recipe <strong>{saveMessage}</strong> saved successfully.
                    </p>
                    <button
                        className="save-notification-close"
                        onClick={() => setSaveMessage('')}
                        aria-label="Dismiss notification"
                    >
                        <X size={18} />
                    </button>
                </div>
            )}

            {saveError && (
                <div
                    ref={notificationRef}
                    className="save-notification save-notification-error"
                    role="alert"
                >
                    <p>{saveError}</p>
                    <button
                        className="save-notification-close"
                        onClick={() => setSaveError('')}
                        aria-label="Dismiss error"
                    >
                        <X size={18} />
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
                            setReturnToMyRecipes(false)
                        }}
                        onEdit={() => {
                            setRecipe(savedRecipe)
                            setSavedRecipe(null)
                            setIsEditingRecipe(true)
                        }}
                        onDelete={() =>
                            handleDeleteRecipe(savedRecipe.id, savedRecipe.name)
                        }
                    />
                ) : showMyRecipes ? (
                    <div className="my-recipes-section">
                        <h2>My Recipes</h2>

                        {deleteMessage && (
                            <div
                                ref={deleteNotificationRef}
                                className="delete-notification"
                                role="status"
                            >
                                <span aria-hidden="true">
                                    <CheckCircle size={20} />
                                </span>
                                <p><strong>{deleteMessage}</strong> was deleted successfully.</p>
                                <button
                                    type="button"
                                    onClick={() => setDeleteMessage('')}
                                    aria-label="Dismiss notification"
                                >
                                    <X size={18} />
                                </button>
                            </div>
                        )}

                        {deleteError && (
                            <div className="delete-error-notification" role="alert">
                                <span aria-hidden="true">
                                    <CircleAlert size={20} />
                                </span>
                                <p>
                                    <strong>Couldn't delete
                                    {' '}{deleteError.replace(". Please try again.", "")}</strong>:
                                    {' '}{myRecipesError}.
                                    {' '}Please try again.
                                </p>
                                <button
                                    type="button"
                                    onClick={() => setDeleteError('')}
                                    aria-label="Dismiss error notification"
                                >
                                    <X size={18} />
                                </button>
                            </div>
                        )}

                        {myRecipesError && (
                            <p role="alert">{myRecipesError}</p>
                        )}

                        <button
                            className="back-button"
                            onClick={() => setShowMyRecipes(false)}
                        >
                            <MoveLeft size={20} strokeWidth={1.8} />
                            Back
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
                                        <div className="recipe-card-actions">
                                            <button
                                                type="button"
                                                className="edit-recipe-button"
                                                onClick={() => {
                                                    console.log('Edit clicked:', item.id, item.name)
                                                    handleEditRecipe(item.id)
                                                }}
                                            >
                                                <Pencil size={20} strokeWidth={1.8} />
                                                Edit
                                            </button>
                                            <button
                                                type="button"
                                                className="delete-recipe-button"
                                                onClick={(event) => {
                                                    console.log('Delete clicked: ', item.id, item.name)
                                                    handleDeleteRecipe(item.id, item.name)
                                                }}
                                            >
                                                <Trash size={20} strokeWidth={1.8} />
                                                Delete
                                            </button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}


                    </div>
                ) : recipe ? (
                    <RecipeReview
                        recipe={recipe}
                        onBack={() => {
                            if (isEditingRecipe) {
                                setIsEditingRecipe(false)
                                setRecipe(null)
                                setShowMyRecipes(returnToMyRecipes)
                                setReturnToMyRecipes(false)
                            } else {
                                setRecipe(null)
                            }
                        }}
                        onSave={handleSave}
                    />
                ) : !showImport ? (
                    <div className="home-actions">
                        <button
                            className="home-action-card"
                            onClick={() => setShowImport(true)}
                        >
                            <span className="home-action-icon" aria-hidden="true">
                                <Import size={24} strokeWidth={1.8} />
                            </span>
                            <span>Import Recipe</span>
                        </button>

                        <button
                            className="home-action-card"
                            onClick={handleShowMyRecipes}
                        >
                            <span className="home-action-icon" aria-hidden="true">
                                <BookOpen size={24} strokeWidth={1.8} />
                            </span>
                            <span>My Recipes</span>
                        </button>
                    </div>
                ) : (
                    <div className="import-recipe-section">
                        <h2>Import Recipe</h2>

                        <div className="import-mode-selector">
                            <button
                                type="button"
                                className={importMode === 'text' ? 'active' : ''}
                                onClick={() => setImportMode('text')}
                            >
                                Import recipe text
                            </button>

                            <button
                                type="button"
                                className={importMode === 'url' ? 'active' : ''}
                                onClick={() => setImportMode('url')}
                            >
                                Import from url
                            </button>
                        </div>

                        {importMode === 'text' ? (
                            <div className="textarea-wrapper">
                                <textarea
                                    placeholder="Paste your recipe here..."
                                    rows="15"
                                    value={recipeText}
                                    onChange={(event) => setRecipeText(event.target.value)}
                                />
                            </div>
                        ) : (
                            <div className="textarea-wrapper">
                                <input
                                    type="url"
                                    placeholder="https://example.com/recipe"
                                    value={recipeUrl}
                                    onChange={(event) => setRecipeUrl(event.target.value)}
                                />
                            </div>
                        )}

                        <div className="import-recipe-actions">
                            <button onClick={() => setShowImport(false)}>
                                <X size={20} strokeWidth={1.8} />
                                Cancel
                            </button>

                            <button onClick={handleImport}>
                                <Import size={20} strokeWidth={1.8} />
                                Import
                            </button>
                        </div>
                    </div>
                )}
            </main>
            {recipeToDelete && (
                <>
                    <p style={{ color: 'red' }}>
                        DEBUG: Delete dialog should be open for {recipeToDelete.name}
                    </p>

                    <div className="delete-dialog-backdrop">
                        <div
                            className="delete-dialog"
                            role="dialog"
                            aria-modal="true"
                            aria-labelledby="delete-dialog-title"
                            aria-describedby="delete-dialog-description"
                        >
                            <div className="delete-dialog-heading">
                                <Trash className="delete-dialog-icon" size={24} />
                                <h2 id="delete-dialog-title">Delete recipe?</h2>
                            </div>

                            <p id="delete-dialog-description">
                                Are you sure you want to delete{' '}
                                <strong>{recipeToDelete.name}</strong>?
                                This action can't be undone.
                            </p>

                            <div className="delete-dialog-actions">
                                <button
                                    type="button"
                                    className="delete-dialog-cancel"
                                    onClick={() => setRecipeToDelete(null)}
                                >
                                    <X size={18} strokeWidth={1.8} />
                                    Cancel
                                </button>

                                <button
                                    type="button"
                                    className="delete-dialog-confirm"
                                    onClick={confirmDeleteRecipe}
                                >
                                    <Trash size={18} strokeWidth={1.8} />
                                    Delete recipe
                                </button>
                            </div>
                        </div>
                    </div>
                </>
            )}
        </div>
    )
}

export default App
