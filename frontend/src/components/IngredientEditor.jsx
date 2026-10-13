import { useState } from 'react'

function IngredientEditor({
    ingredient,
    availableIngredients = [],
    availableIngredientCategories = [],
    onIngredientCreated,
    onIngredientCategoryCreated,
    onChange,
    onRemove
}) {
    const [creatingIngredient, setCreatingIngredient] = useState(false)
    const [newIngredientName, setNewIngredientName] = useState('')
    const [ingredientError, setIngredientError] = useState('')
    const [creatingCategory, setCreatingCategory] = useState(false)
    const [newCategoryName, setNewCategoryName] = useState('')
    const [categoryError, setCategoryError] = useState('')

    const updateField = (field, value) => {
        onChange({
            ...ingredient,
            [field]: value,
        })
    }

    return (
        <div className="ingredient-row">
            <input
                type="number"
                value={ingredient.quantity ?? ''}
                onChange={(event) =>
                    updateField(
                        'quantity',
                        event.target.value === ''
                            ? null
                            : Number(event.target.value)
                    )
                }
                placeholder="Qty"
            />

            <select
                className={!ingredient.unit ? 'placeholder-selected' : ''}
                value={ingredient.unit ?? ''}
                onChange={(event) =>
                    updateField(
                        'unit',
                        event.target.value || null
                    )
                }
            >
                <option value="" disabled>Unit</option>
                <option value="G">g</option>
                <option value="KG">kg</option>
                <option value="ML">ml</option>
                <option value="L">l</option>
                <option value="TSP">tsp</option>
                <option value="TBSP">tbsp</option>
                <option value="CUP">cup</option>
                <option value="OZ">oz</option>
                <option value="LB">lb</option>
                <option value="CLOVE">clove</option>
                <option value="EACH">each</option>
            </select>

            <input
                type="text"
                value={ingredient.ingredientName ?? ''}
                onChange={(event) =>
                    updateField(
                        'ingredientName',
                        event.target.value
                    )
                }
                placeholder="Ingredient"
            />

            <input
                className="ingredient-preparation"
                type="text"
                value={ingredient.preparation ?? ''}
                onChange={(event) =>
                    updateField(
                        'preparation',
                        event.target.value || null
                    )
                }
                placeholder="Preparation"
            />

            <label className="ingredient-optional">
                <input
                    type="checkbox"
                    checked={ingredient.optional ?? false}
                    onChange={(event) =>
                        updateField(
                            'optional',
                            event.target.checked
                        )
                    }
                />
                Optional
            </label>

            <button
                type="button"
                className="remove-ingredient-button"
                onClick={onRemove}
            >
                Remove
            </button>

            <select
                className={`ingredient-match-select ${
                    !ingredient.ingredientId ? 'placeholder-selected' : ''
                }`}
                value={ingredient.ingredientId ?? ''}
                onChange={(event) => {
                    const value = event.target.value

                    if (value === 'new') {
                        setNewIngredientName(ingredient.ingredientName ?? '')
                        setCreatingIngredient(true)
                        updateField('ingredientId', null)
                        return
                    }

                    setCreatingIngredient(false)

                    updateField(
                        'ingredientId',
                        value ? Number(value) : null
                    )
                }}
            >

                <option value="" disabled>Match ingredient</option>

                {availableIngredients.map((availableIngredient) => (
                    <option
                        key={availableIngredient.id}
                        value={availableIngredient.id}
                    >
                        {availableIngredient.name}
                    </option>
                ))}

                <option value="new">+ Create new ingredient</option>
            </select>

            {creatingIngredient && (
                <div className="new-ingredient-form">
                    <input
                        type="text"
                        className="new-ingredient-name"
                        value={newIngredientName}
                        onChange={(event) => {
                            setNewIngredientName(event.target.value)
                            setIngredientError('')
                        }}
                        placeholder="Ingredient name"
                    />

                    {ingredientError && (
                        <p className="form-error ingredient-error">{ingredientError}</p>
                    )}

                    <select
                        className={
                            !(ingredient.defaultUnit ?? ingredient.unit)
                                ? 'placeholder-selected'
                                : ''
                        }
                        value={ingredient.defaultUnit ?? ingredient.unit ?? ''}
                        onChange={(event) => {
                            updateField(
                                'defaultUnit',
                                event.target.value || null
                            )
                            setIngredientError('')
                        }}
                    >
                        <option value="" disabled>Default Unit</option>
                        <option value="G">g</option>
                        <option value="KG">kg</option>
                        <option value="ML">ml</option>
                        <option value="L">l</option>
                        <option value="TSP">tsp</option>
                        <option value="TBSP">tbsp</option>
                        <option value="CUP">cup</option>
                        <option value="OZ">oz</option>
                        <option value="LB">lb</option>
                        <option value="CLOVE">clove</option>
                        <option value="EACH">each</option>
                    </select>

                    <select
                        className={`ingredient-category-match-select ${
                            !ingredient.ingredientCategoryId ? 'placeholder-selected' : ''
                        }`}
                        value={ingredient.ingredientCategoryId ?? ''}
                        onChange={(event) => {
                            const value = event.target.value

                            if (value === 'new') {
                                setCreatingCategory(true)
                                setCategoryError('')
                                return
                            }

                            setCreatingCategory(false)

                            updateField(
                                'ingredientCategoryId',
                                value ? Number(value) : null
                            )
                            setIngredientError('')
                        }}
                    >
                        <option value="" disabled>Ingredient category</option>

                        {availableIngredientCategories.map((category) => (
                            <option
                                key={category.id}
                                value={category.id}
                            >
                                {category.name}
                            </option>
                        ))}

                        <option value="new">+ Create new category</option>
                    </select>

                    {creatingCategory && (
                        <div className="new-category-form">
                            <input
                                type="text"
                                value={newCategoryName}
                                onChange={(event) => {
                                    setNewCategoryName(event.target.value)
                                    setCategoryError('')
                                }}
                                placeholder="New category name"
                            />

                            {categoryError && (
                                <p className="form-error">{categoryError}</p>
                            )}

                            <div className="new-category-actions">
                                <button
                                    type="button"
                                    onClick={async () => {
                                        const name = newCategoryName.trim()

                                        if (!name) {
                                            setCategoryError('Please enter a category name.')
                                            return
                                        }

                                        const existingCategory = availableIngredientCategories.find(
                                            (category) =>
                                                category.name.trim().toLowerCase() === name.toLowerCase()
                                        )

                                        if (existingCategory) {
                                            setCategoryError('A category with this name already exists.')
                                            return
                                        }

                                        try {
                                            const response = await fetch('/api/ingredient-categories', {
                                                method: 'POST',
                                                headers: {
                                                    'Content-Type': 'application/json',
                                                },
                                                body: JSON.stringify({ name }),
                                            })

                                            if (!response.ok) {
                                                if (response.status >= 500) {
                                                    throw new Error(
                                                        `Something went wrong while creating the category. Please try
                                                         again.`
                                                    )
                                                }

                                                const errorText = await response.text()

                                                try {
                                                    const errorData = JSON.parse(errorText)

                                                    throw new Error(
                                                        errorData.message ||
                                                        errorData.detail ||
                                                        errorData.title ||
                                                        `Could not create category (${response.status}). Please check
                                                         the details.`
                                                    )
                                                } catch (error) {
                                                    if (error instanceof SyntaxError) {
                                                        throw new Error(
                                                            errorText || `Failed to create category (${response.status})`
                                                        )
                                                    }

                                                    throw error
                                                }
                                            }

                                            const createdCategory = await response.json()

                                            onIngredientCategoryCreated(createdCategory)
                                            updateField('ingredientCategoryId', createdCategory.id)
                                            setNewCategoryName('')
                                            setCreatingCategory(false)
                                            setCategoryError('')
                                        } catch (error) {
                                            console.error('Failed to create category:', error)
                                            setCategoryError(error.message || 'Failed to create category.')
                                        }
                                    }}
                                >
                                    Create Category
                                </button>

                                <button
                                    type="button"
                                    onClick={() => setCreatingCategory(false)}
                                >
                                    Cancel
                                </button>
                            </div>
                        </div>
                    )}

                    <button
                        type="button"
                        className="create-ingredient-button"
                        onClick={async () => {
                            const name = newIngredientName.trim()

                            if (!name) {
                                setIngredientError('Please enter an ingredient name.')
                                return
                            }

                            const defaultUnit = ingredient.defaultUnit ?? ingredient.unit

                            if (!defaultUnit) {
                                setIngredientError('Please select a default unit.')
                                return
                            }

                            if (!ingredient.ingredientCategoryId) {
                                setIngredientError('Please select or create an ingredient category.')
                                return
                            }

                            try {
                                const response = await fetch('/api/ingredients', {
                                    method: 'POST',
                                    headers: {
                                        'Content-Type': 'application/json',
                                    },
                                    body: JSON.stringify({
                                        name,
                                        defaultUnit,
                                        ingredientCategoryId: ingredient.ingredientCategoryId
                                    }),
                                })

                                if (!response.ok) {
                                    if (response.status >= 500) {
                                        throw new Error(
                                            'Something went wrong while creating the ingredient. Please try again.'
                                        )
                                    }

                                    const errorText = await response.text()

                                    try {
                                        const errorData = JSON.parse(errorText)

                                        throw new Error(
                                            errorData.message ||
                                            errorData.detail ||
                                            errorData.title ||
                                            `Could not create ingredient (${response.status}). Please check the
                                            details.`
                                        )
                                    } catch (error) {
                                        if (error instanceof SyntaxError) {
                                            throw new Error(
                                                errorText || `Could not create ingredient (${response.status}).`
                                            )
                                        }

                                        throw error
                                    }
                                }

                                const createdIngredient = await response.json()

                                onIngredientCreated(createdIngredient)
                                updateField('ingredientId', createdIngredient.id)
                                setCreatingIngredient(false)

                            } catch (error) {
                                console.error('Failed to create ingredient:', error)
                                setIngredientError(error.message || 'Failed to create ingredient.')
                            }
                        }}
                    >
                        Create Ingredient
                    </button>

                    <button
                        type="button"
                        className="cancel-ingredient-button"
                        onClick={() => setCreatingIngredient(false)}
                    >
                        Cancel
                    </button>
                </div>
            )}
        </div>
    )
}

export default IngredientEditor