import InstructionStepEditor from './InstructionStepEditor'

function InstructionSection({
    section,
    sectionPath,
    onUpdateStep,
    onRemoveStep,
    onRemoveSection,
    onUpdateSectionName,
    onAddStep,
    onAddSection
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

                <button
                    type="button"
                    className="add-item-button"
                    onClick={() => onAddStep(sectionPath)}
                >
                    Add step
                </button>

                <button
                    type="button"
                    className="add-item-button"
                    onClick={() => onAddSection(sectionPath)}
                >
                    Add section
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
                    onAddStep={onAddStep}
                    onAddSection={onAddSection}
                />
            ))}
        </div>
    )
}

export default InstructionSection