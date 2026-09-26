import{ useState } from 'react'
import { formatTime } from '../utils/formatTime'
import {
    updateNestedSection,
    removeNestedSection
} from '../utils/updateNestedSection'
import IngredientEditor from './IngredientEditor'
import InstructionStepEditor from './InstructionStepEditor'

function IngredientSection({
    section,
    sectionPath,
    onUpdateIngredient,
    onRemoveIngredient,
    onRemoveSection,
    onUpdateSectionName,
    onAddIngredient
}) {
    return (
        <div className="ingredient-section">
            <div className="ingredient-section-header">
                <input
                    type="text"
                    value={section.name}
                    onChange={(event) =>
                        onUpdateSectionName(
                            sectionPath,
                            event.target.value
                        )
                    }
                />

                <button
                    type="button"
                    className="remove-section-button"
                    onClick={() => onRemoveSection(sectionPath)}
                >
                    Remove section
                </button>
            </div>

            <button
                type="button"
                className="add-item-button"
                onClick={() => onAddIngredient(sectionPath)}
            >
                Add ingredient
            </button>

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
                    onUpdateSectionName={onUpdateSectionName}
                    onAddIngredient={onAddIngredient}
                />
            ))}
        </div>
    )
}

function InstructionSection({
    section,
    sectionPath,
    onUpdateStep,
    onRemoveStep,
    onRemoveSection,
    onUpdateSectionName
}) {
    return (
        <div className="instruction-section">
            <div className="instruction-section-header">
                <input
                    type="text"
                    value={section.name}
                    onChange={(event) =>
                        onUpdateSectionName(
                            sectionPath,
                            event.target.value
                        )
                    }
                />

                <button
                    type="button"
                    className="remove-section-button"
                    onClick={() => onRemoveSection(sectionPath)}
                >
                    Remove section
                </button>
            </div>

            <div className="instruction-list">
                {section.steps.map((step, index) => (
                    <InstructionStepEditor
                        key={index}
                        step={step}
                        stepNumber={index + 1}
                        onChange={(updatedStep) =>
                            onUpdateStep(
                                sectionPath,
                                index,
                                updatedStep
                            )
                        }
                        onRemove={() =>
                            onRemoveStep(
                                sectionPath,
                                index
                            )
                        }
                    />
                ))}
            </div>

            {section.sections.map((nestedSection, index) => (
                <InstructionSection
                    key={index}
                    section={nestedSection}
                    sectionPath={[...sectionPath, index]}
                    onUpdateStep={onUpdateStep}
                    onRemoveStep={onRemoveStep}
                    onRemoveSection={onRemoveSection}
                    onUpdateSectionName={onUpdateSectionName}
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
    const addIngredient = () => {
        setEditedRecipe((current) => ({
            ...current,
            ingredients: [
                ...current.ingredients,
                {
                    quantity: null,
                    unit: null,
                    ingredientName: '',
                    preparation: null,
                    optional: false
                }
            ]
        }))
    }
    const addStep = () => {
        setEditedRecipe((current) => ({
            ...current,
            steps: [
                ...current.steps,
                {
                    instruction: ''
                }
            ]
        }))
    }
    const addIngredientSection = () => {
        setEditedRecipe((current) => ({
            ...current,
            ingredientSections: [
                ...current.ingredientSections,
                {
                    name: 'New section',
                    ingredients: [],
                    sections: []
                }
            ]
        }))
    }
    const updateSectionIngredient = (
        sectionPath,
        ingredientIndex,
        updatedIngredient
    ) => {
        setEditedRecipe((current) => ({
            ...current,
            ingredientSections: updateNestedSection(
                current.ingredientSections,
                sectionPath,
                (section) => ({
                    ...section,
                    ingredients: section.ingredients.map(
                        (ingredient, index) =>
                            index === ingredientIndex
                                ? updatedIngredient
                                : ingredient
                    )
                })
            )
        }))
    }
    const removeSectionIngredient = (
        sectionPath,
        ingredientIndex
    ) => {
        setEditedRecipe((current) => ({
            ...current,
            ingredientSections: updateNestedSection(
                current.ingredientSections,
                sectionPath,
                (section) => ({
                    ...section,
                    ingredients: section.ingredients.filter(
                        (_, index) => index !== ingredientIndex
                    )
                })
            )
        }))
    }
    const addSectionIngredient = (sectionPath) => {
        setEditedRecipe((current) => ({
            ...current,
            ingredientSections: updateNestedSection(
                current.ingredientSections,
                sectionPath,
                (section) => ({
                    ...section,
                    ingredients: [
                        ...section.ingredients,
                        {
                            quantity: null,
                            unit: null,
                            ingredientName: '',
                            preparation: null,
                            optional: false
                        }
                    ]
                })
            )
        }))
    }
    const addInstructionSection = () => {
        setEditedRecipe((current) => ({
            ...current,
            instructionSections: [
                ...current.instructionSections,
                {
                    name: 'New section',
                    steps: [],
                    sections: []
                }
            ]
        }))
    }
    const updateSectionStep = (
        sectionPath,
        stepIndex,
        updatedStep
    ) => {
        setEditedRecipe((current) => ({
            ...current,
            instructionSections: updateNestedSection(
                current.instructionSections,
                sectionPath,
                (section) => ({
                    ...section,
                    steps: section.steps.map(
                        (step, index) =>
                            index === stepIndex
                                ? updatedStep
                                : step
                    )
                })
            )
        }))
    }
    const removeSectionStep = (
        sectionPath,
        stepIndex
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
                            steps: section.steps.filter(
                                (_, index) =>
                                    index !== stepIndex
                            )
                        }
                    }

                    return {
                        ...section,
                        sections: updateSection(
                            section.sections,
                            pathIndex + 1
                        )
                    }
                })
            }

            return {
                ...current,
                instructionSections:updateSection(
                    current.instructionSections,
                    0
                )
            }
        })
    }
    const removeSection = (sectionPath) => {
        setEditedRecipe((current) => ({
            ...current,
            ingredientSections: removeNestedSection(
                current.ingredientSections,
                sectionPath
            )
        }))
    }
    const updateSectionName = (
        sectionPath,
        name
    ) => {
        setEditedRecipe((current) => ({
            ...current,
            ingredientSections: updateNestedSection(
                current.ingredientSections,
                sectionPath,
                (section) => ({
                    ...section,
                    name
                })
            )
        }))
    }
    const updateInstructionSectionName = (
        sectionPath,
        name
    ) => {
        setEditedRecipe((current) => ({
            ...current,
            instructionSections: updateNestedSection(
                current.instructionSections,
                sectionPath,
                (section) => ({
                    ...section,
                    name
                })
            )
        }))
    }
    const removeInstructionSection = (sectionPath) => {
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
                instructionSections: removeFromSections(
                    current.instructionSections,
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

                    <button
                        type="button"
                        className="add-item-button"
                        onClick={addIngredient}
                    >
                        Add ingredient
                    </button>

                    <button
                        type="button"
                        className="add-item-button"
                        onClick={addIngredientSection}
                    >
                        Add section
                    </button>

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
                            onUpdateSectionName={updateSectionName}
                            onAddIngredient={addSectionIngredient}
                        />
                    ))}
                </section>

                <section className="recipe-section">
                    <h2>Method</h2>

                    <button
                        type="button"
                        className="add-item-button"
                        onClick={addStep}
                    >
                        Add step
                    </button>

                    <button
                        type="button"
                        className="add-item-button"
                        onClick={addInstructionSection}
                    >
                        Add section
                    </button>

                    <div className="instruction-list">
                        {editedRecipe.steps.map((step, index) => (
                            <InstructionStepEditor
                                key={index}
                                step={step}
                                stepNumber={index + 1}
                                onChange={(updatedStep) =>
                                    setEditedRecipe((current) => ({
                                        ...current,
                                        steps: current.steps.map(
                                            (currentStep, stepIndex) =>
                                                stepIndex === index
                                                ? updatedStep
                                                : currentStep
                                        )
                                    }))
                                }
                                onRemove={() =>
                                    setEditedRecipe((current) => ({
                                        ...current,
                                        steps: current.steps.filter(
                                            (_, stepIndex) =>
                                                stepIndex !== index
                                        )
                                    }))
                                }
                            />
                        ))}
                    </div>

                    {editedRecipe.instructionSections.map((section, index) => (
                        <InstructionSection
                            key={index}
                            section={section}
                            sectionPath={[index]}
                            onUpdateStep={updateSectionStep}
                            onRemoveStep={removeSectionStep}
                            onRemoveSection={removeInstructionSection}
                            onUpdateSectionName={updateInstructionSectionName}
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