import IngredientEditor from './IngredientEditor'

function IngredientSection({
    section,
    sectionPath,
    onUpdateIngredient,
    onRemoveIngredient,
    onRemoveSection,
    onUpdateSectionName,
    onAddIngredient,
    onAddSection
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

                <button
                    type="button"
                    className="add-item-button"
                    onClick={() => onAddSection(sectionPath)}
                >
                    Add section
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
                    onAddSection={onAddSection}
                />
            ))}
        </div>
    )
}

export default IngredientSection