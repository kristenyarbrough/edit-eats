import { MoveUp, MoveDown, GripVertical } from 'lucide-react'
import IngredientEditor from './IngredientEditor'

function IngredientSection({
    section,
    sectionPath,
    showValidationErrors = false,
    availableIngredients = [],
    availableIngredientCategories = [],
    onIngredientCreated,
    onIngredientCategoryCreated,
    onUpdateIngredient,
    onRemoveIngredient,
    onMoveIngredient,
    onDragStartIngredient,
    onDragOverIngredient,
    onDropIngredient,
    onDragEndIngredient,
    draggedIngredient,
    dragOverIngredient,
    onDragStartSection,
    onDragOverSection,
    onDropSection,
    onDragEndSection,
    draggedIngredientSection,
    dragOverIngredientSection,
    onRemoveSection,
    onUpdateSectionName,
    onAddIngredient,
    onAddSection,
    onMoveSection,
    sectionIndex,
    sectionCount
}) {
    return (
        <div
            className={[
                'ingredient-section',
                draggedIngredientSection &&
                draggedIngredientSection.parentPath.length ===
                    sectionPath.slice(0, -1).length &&
                draggedIngredientSection.parentPath.every(
                    (part, i) => part === sectionPath[i]
                ) &&
                draggedIngredientSection.index === sectionIndex
                    ? 'dragging'
                    : '',
                dragOverIngredientSection &&
                dragOverIngredientSection.parentPath.length ===
                    sectionPath.slice(0, -1).length &&
                dragOverIngredientSection.parentPath.every(
                    (part, i) => part === sectionPath[i]
                ) &&
                dragOverIngredientSection.index === sectionIndex
                    ? 'drag-over'
                    : ''
                ].filter(Boolean).join(' ')}
                onDragOver={(event) => {
                    event.preventDefault()
                    event.stopPropagation()
                    onDragOverSection(sectionPath.slice(0, -1), sectionIndex)
                }}
                onDrop={(event) => {
                    event.preventDefault()
                    event.stopPropagation()
                    onDropSection(sectionPath.slice(0, -1), sectionIndex)
                }}
            >
            <div className="ingredient-section-header">
                <span
                    className="drag-handle"
                    draggable
                    onDragStart={(event) => {
                        event.stopPropagation()
                        onDragStartSection(sectionPath.slice(0, -1), sectionIndex)
                    }}
                    onDragEnd={onDragEndSection}
                    aria-label="Drag to reorder section"
                    title="Drag to reorder section"
                >
                    <GripVertical size={16} />
                </span>
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

                <div className="ingredient-section-actions">
                    <button
                        type="button"
                        className="move-section-button"
                        onClick={() =>
                            onMoveSection(
                                sectionPath.slice(0, -1),
                                sectionIndex,
                                sectionIndex - 1
                            )
                        }
                        disabled={sectionIndex === 0}
                        aria-label="Move section up"
                        title="Move section up"
                    >
                        <MoveUp size={16} />
                    </button>
                    <button
                        type="button"
                        className="move-section-button"
                        onClick={() =>
                            onMoveSection(
                                sectionPath.slice(0, -1),
                                sectionIndex,
                                sectionIndex + 1
                            )
                        }
                        disabled={sectionIndex === sectionCount - 1}
                        aria-label="Move section down"
                        title="Move section down"
                    >
                        <MoveDown size={16} />
                    </button>
                    <button
                        type="button"
                        className="remove-section-button"
                        onClick={() => onRemoveSection(sectionPath)}
                    >
                        Remove section
                    </button>

                    <button
                        type="button"
                        className="add-item-button"
                        onClick={() => onAddSection(sectionPath)}
                    >
                        Add section
                    </button>

                    <button
                        type="button"
                        className="add-item-button"
                        onClick={() => onAddIngredient(sectionPath)}
                    >
                        Add ingredient
                    </button>
                </div>
            </div>


            {section.ingredients.map((ingredient, index) => (
                <IngredientEditor
                    key={index}
                    ingredient={ingredient}
                    showValidationErrors={showValidationErrors}
                    availableIngredients={availableIngredients}
                    availableIngredientCategories={availableIngredientCategories}
                    onIngredientCreated={onIngredientCreated}
                    onIngredientCategoryCreated={onIngredientCategoryCreated}
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
                    onMoveUp={() =>
                        onMoveIngredient(
                            sectionPath,
                            index,
                            index - 1
                        )
                    }
                    onMoveDown={() =>
                        onMoveIngredient(
                            sectionPath,
                            index,
                            index + 1
                        )
                    }
                    canMoveUp={index > 0}
                    canMoveDown={index < section.ingredients.length - 1}
                    onDragStart={(event) =>
                        onDragStartIngredient(event, sectionPath, index)
                    }
                    onDragOver={(event) =>
                        onDragOverIngredient(event, sectionPath, index)
                    }
                    onDrop={(event) =>
                        onDropIngredient(event, sectionPath, index)
                    }
                    onDragEnd={onDragEndIngredient}
                    isDragging={
                        draggedIngredient?.sectionPath === sectionPath.join('.') &&
                        draggedIngredient?.index === index
                    }
                    isDragOver={
                        draggedIngredient?.sectionPath === sectionPath.join('.') &&
                        dragOverIngredient?.sectionPath === sectionPath.join('.') &&
                        dragOverIngredient?.index === index
                    }
                />
            ))}

            {section.sections.map((nestedSection, index) => (
                <IngredientSection
                    key={index}
                    section={nestedSection}
                    sectionPath={[...sectionPath, index]}
                    showValidationErrors={showValidationErrors}
                    availableIngredients={availableIngredients}
                    availableIngredientCategories={availableIngredientCategories}
                    onIngredientCreated={onIngredientCreated}
                    onIngredientCategoryCreated={onIngredientCategoryCreated}
                    onUpdateIngredient={onUpdateIngredient}
                    onRemoveIngredient={onRemoveIngredient}
                    onMoveIngredient={onMoveIngredient}
                    onDragStartIngredient={onDragStartIngredient}
                    onDragOverIngredient={onDragOverIngredient}
                    onDropIngredient={onDropIngredient}
                    onDragEndIngredient={onDragEndIngredient}
                    draggedIngredient={draggedIngredient}
                    dragOverIngredient={dragOverIngredient}
                    onRemoveSection={onRemoveSection}
                    onUpdateSectionName={onUpdateSectionName}
                    onAddIngredient={onAddIngredient}
                    onAddSection={onAddSection}
                    sectionIndex={index}
                    sectionCount={section.sections.length}
                    onMoveSection={onMoveSection}
                />
            ))}
        </div>
    )
}

export default IngredientSection