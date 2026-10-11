import IngredientEditor from './IngredientEditor'

function IngredientSection({
    section,
    sectionPath,
    availableIngredients = [],
    availableIngredientCategories = [],
    onIngredientCreated,
    onIngredientCategoryCreated,
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

                <div className="ingredient-section-actions">
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
                />
            ))}

            {section.sections.map((nestedSection, index) => (
                <IngredientSection
                    key={index}
                    section={nestedSection}
                    sectionPath={[...sectionPath, index]}
                    availableIngredients={availableIngredients}
                    availableIngredientCategories={availableIngredientCategories}
                    onIngredientCreated={onIngredientCreated}
                    onIngredientCategoryCreated={onIngredientCategoryCreated}
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