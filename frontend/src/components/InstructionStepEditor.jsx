function InstructionStepEditor({
    step,
    stepNumber,
    onChange,
    onRemove,
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

            <button
                type="button"
                className="remove-step-button"
                onClick={onRemove}
            >
                Remove
            </button>
        </div>
    )
}

export default InstructionStepEditor