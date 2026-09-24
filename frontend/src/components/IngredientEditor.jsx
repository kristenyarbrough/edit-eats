function IngredientEditor({ ingredient, onChange, onRemove }) {
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
                placeHolder="Qty"
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
                placeHolder="Ingredient"
            />

            <input
                type="text"
                value={ingredient.preparation ?? ''}
                onChange={(event) =>
                    updateField(
                        'preparation',
                        event.target.value || null
                    )
                }
                placeHolder="Preparation"
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