import{ useState } from 'react'
import {
    updateNestedSection,
    removeNestedSection,
    addNestedSection
} from '../utils/nestedSectionUtils'
import { moveItem } from '../utils/reorderUtils'
import IngredientEditor from './IngredientEditor'
import InstructionStepEditor from './InstructionStepEditor'
import IngredientSection from './IngredientSection'
import InstructionSection from './InstructionSection'
import TimeEditor from './TimeEditor'

function RecipeReview ({ recipe, onBack, onSave }) {
    const [editedRecipe, setEditedRecipe] = useState(recipe)
    const [draggedStepIndex, setDraggedStepIndex] = useState(null)
    const [dragOverStepIndex, setDragOverStepIndex] = useState(null)
    const [draggedSectionIndex, setDraggedSectionIndex] = useState(null)
    const [dragOverSectionIndex, setDragOverSectionIndex] = useState(null)
    const updateRecipeField = (field, value) => {
        setEditedRecipe((current) => {
            const updatedRecipe = {
                ...current,
                [field]: value,
            }

            if (field === 'prepMinutes' || field === 'cookMinutes' || field === 'passiveMinutes') {
                const prep = field === 'prepMinutes'
                    ? value
                    : current.prepMinutes

                const cook = field === 'cookMinutes'
                    ? value
                    : current.cookMinutes

                const passive = field === 'passiveMinutes'
                    ? value
                    : current.passiveMinutes

                updatedRecipe.totalMinutes =
                    (prep || 0) + (cook || 0) + (passive || 0)
            }

            return updatedRecipe
        })
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
                    id: crypto.randomUUID(),
                    instruction: ''
                }
            ]
        }))
    }
    const moveStep = (fromIndex, toIndex) => {
        setEditedRecipe((current) => ({
            ...current,
            steps: moveItem(
                current.steps,
                fromIndex,
                toIndex
            )
        }))
    }
    const moveInstructionSection = (sectionPath, fromIndex, toIndex) => {
        setEditedRecipe((currentRecipe) => {
            if (sectionPath.length === 0) {
                return {
                   ...currentRecipe,
                    instructionSections: moveItem(
                        currentRecipe.instructionSections,
                        fromIndex,
                        toIndex
                    )
                }
            }

            return {
                ...currentRecipe,
                instructionSections: updateNestedSection(
                    currentRecipe.instructionSections,
                    sectionPath,
                    (section) => ({
                        ...section,
                        sections: moveItem(
                            section.sections,
                            fromIndex,
                            toIndex
                        )
                    })
                )
            }
        })
    }
    const addIngredientSection = (sectionPath = null) => {
        const newSection = {
            name: 'New section',
            ingredients: [
                {
                    quantity: null,
                    unit: null,
                    ingredientName: '',
                    preparation: null,
                    optional: false
                }
            ],
            sections: []
        }

        setEditedRecipe((current) => {
            if (sectionPath === null) {
                return {
                    ...current,
                    ingredientSections: [
                        ...current.ingredientSections,
                        newSection
                    ]
                }
            }

            return {
                ...current,
                ingredientSections: addNestedSection(
                    current.ingredientSections,
                    sectionPath,
                    newSection
                )
            }
        })
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
    const addInstructionSection = (sectionPath = null) => {
        const newSection = {
            id: crypto.randomUUID(),
            name: 'New section',
            steps: [
                {
                    id: crypto.randomUUID(),
                    instruction: ''
                }
            ],
            sections: []
        }

        setEditedRecipe((current) => {
            if (sectionPath === null) {
                return {
                    ...current,
                    instructionSections: [
                        ...current.instructionSections,
                        newSection
                    ]
                }
            }

            return{
                ...current,
                instructionSections: addNestedSection(
                    current.instructionSections,
                    sectionPath,
                    newSection
                )
            }
        })
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
        setEditedRecipe((current) => ({
            ...current,
            instructionSections: updateNestedSection(
                current.instructionSections,
                sectionPath,
                (section) => ({
                    ...section,
                    steps: section.steps.filter(
                        (_, index) => index !== stepIndex
                    )
                })
            )
        }))
    }
    const addSectionStep = (sectionPath) => {
        setEditedRecipe((current) => ({
            ...current,
            instructionSections: updateNestedSection(
                current.instructionSections,
                sectionPath,
                (section) => ({
                    ...section,
                    steps: [
                        ...(section.steps ?? []),
                        {
                            id: crypto.randomUUID(),
                            instruction: ''
                        }
                    ]
                })
            )
        }))
    }
    const moveSectionStep = (sectionPath, fromIndex, toIndex) => {
        setEditedRecipe((current) => ({
            ...current,
            instructionSections: updateNestedSection(
                current.instructionSections,
                sectionPath,
                (section) => ({
                    ...section,
                    steps: moveItem(
                        section.steps,
                        fromIndex,
                        toIndex
                    )
                })
            )
        }))
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
        setEditedRecipe((current) => ({
            ...current,
            instructionSections: removeNestedSection(
                current.instructionSections,
                sectionPath
            )
        }))
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
{/* //                         {editedRecipe.servings != null && ( */}
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
{/*                         )} */}

                        <TimeEditor
                            label="Prep:"
                            value={editedRecipe.prepMinutes}
                            onChange={(value) =>
                                updateRecipeField('prepMinutes', value)
                            }
                        />

                        <TimeEditor
                            label="Cook:"
                            value={editedRecipe.cookMinutes}
                            onChange={(value) =>
                                updateRecipeField('cookMinutes', value)
                            }
                        />

                        <TimeEditor
                            label="Inactive:"
                            value={editedRecipe.passiveMinutes}
                            onChange={(value) =>
                                updateRecipeField('passiveMinutes', value)
                            }
                        />

                        <TimeEditor
                            label="Total:"
                            value={editedRecipe.totalMinutes}
                            onChange={(value) =>
                                updateRecipeField('totalMinutes', value)
                            }
                        />
                    </div>
                </div>

                <section className="recipe-section">
                    <h2>Ingredients</h2>

                    <div className="recipe-section-actions">
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
                            onClick={() => addIngredientSection()}
                        >
                            Add section
                        </button>
                    </div>

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
                            onAddSection={addIngredientSection}
                        />
                    ))}
                </section>

                <section className="recipe-section">
                    <h2>Method</h2>

                    <div className="instruction-actions">
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
                            onClick={() => addInstructionSection()}
                        >
                            Add section
                        </button>
                    </div>

                    <div className="instruction-list">
                        {editedRecipe.steps.map((step, index) => (
                            <InstructionStepEditor
                                key={step.id}
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

                                onMoveUp={() => moveStep(index, index - 1)}

                                onMoveDown={() => moveStep(index, index + 1)}

                                canMoveUp={index > 0}

                                canMoveDown={index < editedRecipe.steps.length - 1}

                                onDragStart={(event) => {
                                    setDraggedStepIndex(index)

                                    event.dataTransfer.effectAllowed = 'move'
                                    event.dataTransfer.setData(
                                        'application/x-edit-eats-step',
                                        index.toString()
                                    )
                                }}

                                onDragOver={(event) => {
                                    if (
                                        !event.dataTransfer.types.includes(
                                            'application/x-edit-eats-step'
                                        )
                                    ) {
                                        return
                                    }

                                    event.preventDefault()
                                    setDragOverStepIndex(index)

                                    event.dataTransfer.dropEffect = 'move'
                                }}

                                onDrop={(event) => {
                                    event.preventDefault()

                                    const draggedIndex = Number(
                                        event.dataTransfer.getData('application/x-edit-eats-step')
                                    )

                                    moveStep(
                                        draggedIndex,
                                        index
                                    )

                                    setDraggedStepIndex(null)
                                    setDragOverStepIndex(null)
                                }}

                                onDragEnd={() => {
                                    setDraggedStepIndex(null)
                                    setDragOverStepIndex(null)
                                }}

                                isDragging={draggedStepIndex === index}
                                isDragOver={
                                    dragOverStepIndex === index &&
                                    draggedStepIndex !== index
                                }
                            />
                        ))}
                    </div>

                    {editedRecipe.instructionSections.map((section, index) => (
                        <InstructionSection
                            key={section.id}
                            section={section}
                            sectionPath={[index]}
                            sectionIndex={index}
                            sectionCount={editedRecipe.instructionSections.length}
                            onUpdateStep={updateSectionStep}
                            onRemoveStep={removeSectionStep}
                            onRemoveSection={removeInstructionSection}
                            onUpdateSectionName={updateInstructionSectionName}
                            onAddStep={addSectionStep}
                            onAddSection={addInstructionSection}
                            onMoveStep={moveSectionStep}
                            onMoveSection={moveInstructionSection}

                            onDragStart={() => {
                                setDraggedSectionIndex(index)
                            }}

                            onDragOver={() => {
                                setDragOverSectionIndex(index)
                            }}

                            onDrop={() => {
                                moveInstructionSection(
                                    draggedSectionIndex,
                                    index
                                )

                                setDraggedSectionIndex(null)
                                setDragOverSectionIndex(null)
                            }}

                            onDragEnd={() => {
                                setDraggedSectionIndex(null)
                                setDragOverSectionIndex(null)
                            }}

                            isDragging={draggedSectionIndex === index}

                            isDragOver={
                                dragOverSectionIndex === index &&
                                draggedSectionIndex !== index
                            }
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