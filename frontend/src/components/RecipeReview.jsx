import{ useState } from 'react'
import { formatTime } from '../utils/formatTime'
import IngredientEditor from './IngredientEditor'

function IngredientSection({
    section,
    sectionPath,
    onUpdateIngredient,
    onRemoveIngredient,
    onRemoveSection
}) {
    return (
        <div className="ingredient-section">
            <div className="ingredient-section-header">
                <h3>{section.name}</h3>

                <button
                    type="button"
                    className="remove-section-button"
                    onClick={() => onRemoveSection(sectionPath)}
                >
                    Remove section
                </button>
            </div>

            {section.ingredients.map((ingredient, index) => (
                <IngredientEditor
                    key={index}
                    ingredient={ingredient}
                    onChange={(updatedIngredient) =>
                        onUpdateIngredient(
                            sectionPath,
                            index,
                            updatedIngredient
                        )
                    }
                    onRemove={() =>
                        onRemoveIngredient(
                            sectionPath,
                            index
                        )
                    }
                />
            ))}

            {section.sections.map((nestedSection, index) => (
                <IngredientSection
                    key={index}
                    section={nestedSection}
                    sectionPath={[...sectionPath, index]}
                    onUpdateIngredient={onUpdateIngredient}
                    onRemoveIngredient={onRemoveIngredient}
                    onRemoveSection={onRemoveSection}
                />
            ))}
        </div>
    )
}

function InstructionSection({ section }) {
    return (
        <div className="instruction-section">
            <h3>{section.name}</h3>

            <div className="instruction-list">
                {section.steps.map((step) => (
                    <p key={step.stepNumber}>
                        <span className="step-number">
                            {step.stepNumber}.
                         </span>
                         {step.instruction}
                    </p>
                ))}
            </div>

            {section.sections.map((nestedSection, index) => (
                <InstructionSection
                    key={index}
                    section={nestedSection}
                />
            ))}
        </div>
    )
}

function RecipeReview ({ recipe, onBack, onSave }) {
    const [editedRecipe, setEditedRecipe] = useState(recipe)
    const updateRecipeField = (field, value) => {
        setEditedRecipe((current) => ({
            ...current,
            [field]: value,
        }))
    }
    const updateIngredient = (index, updatedIngredient) => {
        setEditedRecipe((current) => ({
            ...current,
            ingredients: current.ingredients.map(
                (ingredient, ingredientIndex) =>
                    ingredientIndex === index
                    ? updatedIngredient
                    : ingredient
            ),
        }))
    }
    const removeIngredient = (index) => {
        setEditedRecipe((current) => ({
            ...current,
            ingredients: current.ingredients.filter(
                (_, ingredientIndex) => ingredientIndex !== index
            ),
        }))
    }
    const updateSectionIngredient = (
        sectionPath,
        ingredientIndex,
        updatedIngredient
    ) => {
        setEditedRecipe((current) => {
            const updateSection = (sections, pathIndex) => {
                return sections.map((section, index) => {
                    if (index !== sectionPath[pathIndex]) {
                        return section
                    }

                    if (pathIndex === sectionPath.length - 1) {
                        return {
                            ...section,
                            ingredients: section.ingredients.map(
                                (ingredient, index) =>
                                    index === ingredientIndex
                                        ? updatedIngredient
                                        : ingredient
                            ),
                        }
                    }

                    return {
                        ...section,
                        sections: updateSection(
                            section.sections,
                            pathIndex + 1
                        ),
                    }
                })
            }

            return {
                ...current,
                ingredientSections: updateSection(
                    current.ingredientSections,
                    0
                ),
            }
        })
    }
    const removeSectionIngredient = (
        sectionPath,
        ingredientIndex
    ) => {
        setEditedRecipe((current) => {
            const updateSection = (sections, pathIndex) => {
                return sections.map((section, index) => {
                    if (index !== sectionPath[pathIndex]) {
                        return section
                    }

                    if (pathIndex === sectionPath.length - 1) {
                        return {
                            ...section,
                            ingredients: section.ingredients.filter(
                                (_, index) =>
                                    index !== ingredientIndex
                            ),
                        }
                    }

                    return {
                        ...section,
                        sections: updateSection(
                            section.sections,
                            pathIndex + 1
                        ),
                    }
                })
            }

            return {
                ...current,
                ingredientSections: updateSection(
                    current.ingredientSections,
                    0
                ),
            }
        })
    }
    const removeSection = (sectionPath) => {
        setEditedRecipe((current) => {
            const removeFromSections = (sections, pathIndex) => {
                const targetIndex = sectionPath[pathIndex]

                if (pathIndex === sectionPath.length - 1) {
                    return sections.filter(
                        (_, index) => index !== targetIndex
                    )
                }

                return sections.map((section, index) => {
                    if (index !== targetIndex) {
                        return section
                    }

                    return {
                        ...section,
                        sections: removeFromSections(
                            section.sections,
                            pathIndex + 1
                        ),
                    }
                })
            }

            return {
                ...current,
                ingredientSections: removeFromSections(
                    current.ingredientSections,
                    0
                ),
            }
        })
    }

    return (
        <div className="recipe-review">
            <div className="recipe-card">
                <div className="recipe-header">
                    <p className="recipe-review-label">Review Recipe</p>

                    <input
                        className="recipe-name-input"
                        type="text"
                        value={editedRecipe.name}
                        onChange={(event) =>
                            updateRecipeField('name', event.target.value)
                        }
                    />

                    <div className="recipe-meta">
                        {editedRecipe.servings != null && (
                            <label className="recipe-meta-field">
                                Serves:
                                <input
                                    type="number"
                                    min="1"
                                    value={editedRecipe.servings}
                                    onChange={(event) =>
                                        updateRecipeField(
                                            'servings',
                                            event.target.value === ''
                                                ? ''
                                                : Number(event.target.value)
                                        )
                                    }
                                />
                                people
                            </label>
                        )}

                        {recipe.prepMinutes != null && (
                            <span>Prep: {formatTime(recipe.prepMinutes)}</span>
                        )}

                        {recipe.cookMinutes != null && (
                            <span>Cook: {formatTime(recipe.cookMinutes)}</span>
                        )}

                        {recipe.totalMinutes != null && (
                            <span>Total: {formatTime(recipe.totalMinutes)}</span>
                        )}
                    </div>
                </div>

                <section className="recipe-section">
                    <h2>Ingredients</h2>

                    <div className="ingredient-list">
                        {editedRecipe.ingredients.map((ingredient, index) => (
                            <IngredientEditor
                                key={index}
                                ingredient={ingredient}
                                onChange={(updatedIngredient) =>
                                    updateIngredient(
                                        index,
                                        updatedIngredient
                                    )
                                }
                                onRemove={() => removeIngredient(index)}
                            />
                        ))}
                    </div>

                    {editedRecipe.ingredientSections.map((section, index) => (
                        <IngredientSection
                            key={index}
                            section={section}
                            sectionPath={[index]}
                            onUpdateIngredient={updateSectionIngredient}
                            onRemoveIngredient={removeSectionIngredient}
                            onRemoveSection={removeSection}
                        />
                    ))}
                </section>

                <section className="recipe-section">
                    <h2>Method</h2>

                    <div className="instruction-list">
                        {recipe.steps.map((step) => (
                            <p key={step.stepNumber}>
                                <span className="step-number">
                                    {step.stepNumber}.
                                </span>
                                {step.instruction}
                            </p>
                        ))}
                    </div>

                    {recipe.instructionSections.map((section, index) => (
                        <InstructionSection
                            key={index}
                            section={section}
                        />
                    ))}
                </section>

                <div>
                    <button onClick={onBack}>
                        Back
                    </button>

                    <button onClick={() => onSave(editedRecipe)}>
                        Save Recipe
                    </button>
                </div>
            </div>
        </div>
    )
}

export default RecipeReview