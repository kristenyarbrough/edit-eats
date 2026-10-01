function InstructionStepEditor({
    step,
    stepNumber,
    onChange,
    onRemove,
    onMoveUp,
    onMoveDown,
    canMoveUp,
    canMoveDown,
    onDragStart,
    onDragOver,
    onDrop,
    onDragEnd,
    isDragging,
    isDragOver
}) {
    return (
        <div
            className={`instruction-step-row ${
                isDragging ? 'dragging' : ''
            } ${
                isDragOver ? 'drag-over' : ''
            }`}
            onDragOver={onDragOver}
            onDrop={onDrop}
        >

            <span
                className="drag-handle"
                draggable="true"
                onDragStart={onDragStart}
                onDragEnd={onDragEnd}
            >
{/*                 ☰ */}
                ⋮⋮
            </span>

            <span className="step-number">
                {stepNumber}.
            </span>

            <input
                type="text"
                value={step.instruction ?? ''}
                onChange={(event) =>
                    onChange({
                        ...step,
                        instruction: event.target.value
                    })
                }
            />

            <div className="step-actions">
                <button
                    type="button"
                    className="move-step-button"
                    onClick={onMoveUp}
                    disabled={!canMoveUp}
                    aria-label={`Move step ${stepNumber} up`}
                >
                    ↑
                </button>

                <button
                    type="button"
                    className="move-step-button"
                    onClick={onMoveDown}
                    disabled={!canMoveDown}
                    aria-label={`Move step ${stepNumber} down`}
                >
                    ↓
                </button>

                <button
                    type="button"
                    className="remove-step-button"
                    onClick={onRemove}
                >
                    Remove
                </button>
            </div>
        </div>
    )
}

export default InstructionStepEditor