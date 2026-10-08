import { useState } from 'react'

function IngredientEditor({
    ingredient,
    availableIngredients = [],
    availableIngredientCategories = [],
    onIngredientCreated,
    onChange,
    onRemove
}) {
    const [creatingIngredient, setCreatingIngredient] = useState(false)

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
                        onChange={(event) =>
                            updateField(
                                'ingredientCategoryId',
                                event.target.value
                                    ? Number(event.target.value)
                                    : null
                            )
                        }
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
                    </select>

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
        </div>
    )
}

export default IngredientEditor