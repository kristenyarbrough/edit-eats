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
                value={ingredient.unit ?? ''}
                onChange={(event) =>
                    updateField(
                        'unit',
                        event.target.value || null
                    )
                }
            >
                <option value="">Unit</option>
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
                value={ingredient.ingredientId ?? ''}
                onChange={(event) => {
                    const value = event.target.value

                    if (value === 'new') {
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

                <option value="">Match ingredient</option>

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
                        value={ingredient.ingredientName ?? ''}
                        onChange={(event) =>
                            updateField(
                                'ingredientName',
                                event.target.value
                            )
                        }
                        placeholder="Ingredient name"
                    />

                    <select
                        value={ingredient.defaultUnit ?? ingredient.unit ?? ''}
                        onChange={(event) =>
                            updateField(
                                'defaultUnit',
                                event.target.value || null
                            )
                        }
                    >
                        <option value="">Default Unit</option>
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
                        }}
                    >
                        <option value="">Ingredient category</option>

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
                                            const errorText = await response.text()
                                            throw new Error(
                                                errorText || `Failed to create category (${response.status})`
                                            )
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
                    )}

                    <button
                        type="button"
                        onClick={async () => {
                            try {
                                const response = await fetch('/api/ingredients', {
                                    method: 'POST',
                                    headers: {
                                        'Content-Type': 'application/json',
                                    },
                                    body: JSON.stringify({
                                        name: ingredient.ingredientName,
                                        defaultUnit: ingredient.defaultUnit ?? ingredient.unit,
                                        ingredientCategoryId: ingredient.ingredientCategoryId
                                    }),
                                })

                                if (!response.ok) {
                                    const errorText = await response.text()
                                    throw new Error(
                                        errorText || `Failed to create ingredient (${response.status})`
                                    )
                                }

                                const createdIngredient = await response.json()

                                onIngredientCreated(createdIngredient)
                                updateField('ingredientId', createdIngredient.id)
                                setCreatingIngredient(false)

                            } catch (error) {
                                console.error('Failed to create ingredient:', error)
                            }
                        }}
                    >
                        Create Ingredient
                    </button>
                </div>
            )}
        </div>
    )
}

export default IngredientEditor