import { useState } from 'react'
import './RecipeView.css'
import { formatTime } from '../utils/formatTime'

function IngredientSection({ section, checkedIngredients, onIngredientCheck }) {
    return (
        <div className="recipe-view-section">
            {section.name && <h3>{section.name}</h3>}

            {section.ingredients?.length > 0 && (
                <ul>
                    {section.ingredients.map((ingredient, index) => {
                        const key = `${ingredient.ingredientId ??
                            'ingredient'}-${ingredient.ingredientName}-${index}`
                        const isChecked = Boolean(checkedIngredients[key])

                        return (
                            <li key={key}>
                                <label>
                                    <input
                                        type="checkbox"
                                        checked={isChecked}
                                        onChange={() => onIngredientCheck(key)}
                                    />
                                    <span className={isChecked ? 'recipe-item-completed' : ''}>
                                        {ingredient.quantity != null && `${ingredient.quantity} `}
                                        {ingredient.unit && `${ingredient.unit.toLowerCase()} `}
                                        {ingredient.ingredientName}
                                        {ingredient.preparation && `, ${ingredient.preparation}`}
                                        {ingredient.optional && ' (optional)'}
                                    </span>
                                </label>
                            </li>
                        )
                    })}
                </ul>
            )}

            {section.sections?.map((nestedSection, index) => (
                <IngredientSection
                    key={nestedSection.name ?? index}
                    section={nestedSection}
                    checkedIngredients={checkedIngredients}
                    onIngredientCheck={onIngredientCheck}
                />
            ))}
        </div>
    )
}

function InstructionSection({ section, completedSteps, onStepClick }) {
    return (
        <div className="recipe-view-section">
            {section.name && <h3>{section.name}</h3>}

            {section.steps?.length > 0 && (
                <ol>
                    {section.steps.map((step, index) => {
                        const key = `${section.name ?? 'section'}-${step.stepNumber
                            ?? index}-${step.instruction}`
                        const isCompleted = Boolean(completedSteps[key])

                        return (
                            <li
                                key={key}
                                onClick={() => onStepClick(key)}
                            >
                                <span className="recipe-step-number">
                                    {index + 1}
                                </span>
                                <span className={isCompleted ? 'recipe-item-completed' : ''}>
                                    {step.instruction}
                                </span>
                            </li>
                        )
                    })}
                </ol>
            )}

            {section.sections?.map((nestedSection, index) => (
                <InstructionSection
                    key={nestedSection.name ?? index}
                    section={nestedSection}
                    completedSteps={completedSteps}
                    onStepClick={onStepClick}
                />
            ))}
        </div>
    )
}

function RecipeView({ recipe, onBack }) {
    const [checkedIngredients, setCheckedIngredients] = useState({})
    const [completedSteps, setCompletedSteps] = useState({})
    const handleIngredientCheck = (key) => {
        setCheckedIngredients((previous) => ({
            ...previous,
            [key]: !previous[key],
        }))
    }
    const handleStepClick = (key) => {
        setCompletedSteps((previous) => ({
            ...previous,
            [key]: !previous[key],
        }))
    }
    return(
        <div className="recipe-view">
            <button className="recipe-back-button" onClick={onBack}>
                ← Back to recipes
            </button>

            <h2>{recipe.name}</h2>

            {recipe.imageUrl && (
                <img
                    className="recipe-view-image"
                    src={recipe.imageUrl}
                    alt={recipe.name}
                    onError={(event) => {
                        event.currentTarget.style.display = 'none'
                    }}
                />
            )}

            <div className="recipe-view-details">
                {recipe.prepMinutes != null && (
                    <div className="recipe-detail">
                        <span className="recipe-detail-label">Prep: </span>
                        <strong>{formatTime(recipe.prepMinutes)}</strong>
                    </div>
                )}
                {recipe.cookMinutes != null && (
                    <div className="recipe-detail">
                        <span className="recipe-detail-label">Cook: </span>
                        <strong>{formatTime(recipe.cookMinutes)}</strong>
                    </div>
                )}
                {recipe.passiveMinutes != null && (
                    <div className="recipe-detail">
                        <span className="recipe-detail-label">Inactive: </span>
                        <strong>{formatTime(recipe.passiveMinutes)}</strong>
                    </div>
                )}
                {recipe.totalMinutes && (
                    <div className="recipe-detail">
                        <span className="recipe-detail-label">Total: </span>
                        <strong>{formatTime(recipe.totalMinutes)}</strong>
                    </div>
                )}
                {recipe.servings != null && (
                    <div className="recipe-detail">
                        <span className="recipe-detail-label">Serves: </span>
                        <strong>{recipe.servings}</strong>
                    </div>
                )}
                {recipe.difficulty && (
                    <div className="recipe-detail">
                        <span className="recipe-detail-label">Difficulty: </span>
                        <strong>
                            {recipe.difficulty.charAt(0) + recipe.difficulty.slice(1).toLowerCase()}
                        </strong>
                    </div>
                )}
            </div>

            {recipe.categories?.length > 0 && (
                <div className="recipe-view-categories">
                    {recipe.categories.map((category, index) => (
                        <span key={category.id ?? index}>
                            {category.name}
                        </span>
                    ))}
                </div>
            )}

            {recipe.sourceUrl && (
                <p className="recipe-view-source">
                    Original recipe:{" "}
                    <a
                        href={recipe.sourceUrl}
                        target="_blank"
                        rel="noreferrer"
                    >
                        View source ↗
                    </a>
                </p>
            )}

            <section>
                <h2>Ingredients</h2>
                {recipe.ingredients?.length > 0 && (
                    <ul>
                        {recipe.ingredients.map((ingredient, index) => {
                            const key = `${ingredient.ingredientId ??
                                'ingredient'}-${ingredient.ingredientName}-${index}`
                            const isChecked = Boolean(checkedIngredients[key])

                            return (
                                <li key={key}>
                                    <label>
                                        <input
                                            type="checkbox"
                                            checked={isChecked}
                                            onChange={() => handleIngredientCheck(key)}
                                        />
                                        <span className={isChecked ? 'recipe-item-completed' : ''}>
                                            {ingredient.quantity != null && `${ingredient.quantity} `}
                                            {ingredient.unit && `${ingredient.unit.toLowerCase()} `}
                                            {ingredient.ingredientName}
                                            {ingredient.preparation && `, ${ingredient.preparation}`}
                                            {ingredient.optional && ' (optional)'}
                                        </span>
                                    </label>
                                </li>
                            )
                        })}
                    </ul>
                )}
                {recipe.ingredientSections?.map((section, index) => (
                    <IngredientSection
                        key={section.name ?? index}
                        section={section}
                        checkedIngredients={checkedIngredients}
                        onIngredientCheck={handleIngredientCheck}
                    />
                ))}
            </section>

            <section>
                <h2>Method</h2>
                {recipe.steps?.length > 0 && (
                    <ol>
                        {recipe.steps?.map((step, index) => {
                            const key = `top-level-${step.stepNumber ?? index}-${step.instruction}`
                            const isCompleted = Boolean(completedSteps[key])

                            return (
                                <li
                                    key={key}
                                    onClick={() => handleStepClick(key)}
                                >
                                    <span className="recipe-step-number">
                                        {index + 1}
                                    </span>
                                    <span className={isCompleted ? 'recipe-item-completed' : ''}>
                                        {step.instruction}
                                    </span>
                                </li>
                            )
                        })}
                    </ol>
                )}
                {recipe.instructionSections?.map((section, index) => (
                    <InstructionSection
                        key={section.name ?? index}
                        section={section}
                        completedSteps={completedSteps}
                        onStepClick={handleStepClick}
                    />
                ))}
            </section>

            {recipe.storageInstructions && (
                <section>
                    <h2>Storage:</h2>
                    <p>{recipe.storageInstructions}</p>
                </section>
            )}

            {recipe.freezerInstructions && (
                <section>
                    <h2>Freezing:</h2>
                    <p>{recipe.freezerInstructions}</p>
                </section>
            )}
        </div>
    )
}

export default RecipeView