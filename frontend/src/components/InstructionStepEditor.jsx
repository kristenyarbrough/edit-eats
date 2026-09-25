function InstructionStepEditor({
    step,
    stepNumber,
    onChange,
    onRemove
}) {
    return (
        <div className="instruction-step-row">
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