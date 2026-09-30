import { useState } from 'react'
import InstructionStepEditor from './InstructionStepEditor'

function InstructionSection({
    section,
    sectionPath,
    onUpdateStep,
    onRemoveStep,
    onRemoveSection,
    onUpdateSectionName,
    onAddStep,
    onAddSection,
    onMoveStep,
    onDragStart,
    onDragOver,
    onDrop,
    onDragEnd,
    isDragging,
    isDragOver
}) {
    const [draggedStepIndex, setDraggedStepIndex] = useState(null)
    const [dragOverStepIndex, setDragOverStepIndex] = useState(null)
    return (
        <>
            {isDragOver && (
                <div className="section-drop-indicator" />
            )}

            <div
                className={`instruction-section ${
                    isDragging ? 'dragging' : ''
                }`}
                onDragOver={(event) => {
                    event.preventDefault()

                    if (
                        event.dataTransfer.types.includes(
                            'application/x-edit-eats-section'
                        )
                    ) {
                        onDragOver()
                    }
                }}
                onDrop={(event) => {
                    event.preventDefault()

                    if (
                        event.dataTransfer.types.includes(
                            'application/x-edit-eats-section'
                        )
                    ) {
                        onDrop()
                    }
                }}
            >
                <div className="instruction-section-header">
                    <span
                        className="drag-handle"
                        draggable="true"
                        onDragStart={(event) => {
                            event.dataTransfer.effectAllowed = 'move'
                            event.dataTransfer.setData(
                                'application/x-edit-eats-section',
                                sectionPath[sectionPath.length - 1].toString()
                            )

                            onDragStart()
                        }}
                        onDragEnd={onDragEnd}
                    >
                        ⋮⋮
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

                    <div className="instruction-section-actions">
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
                </div>

                <div className="instruction-list">
                    {section.steps.map((step, index) => (
                        <InstructionStepEditor
                            key={step.id}
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
                            onDrop ={(event) => {
                                if (
                                    !event.dataTransfer.types.includes(
                                        'application/x-edit-eats-step'
                                    )
                                ) {
                                    return
                                }

                                event.preventDefault()

                                const draggedIndex = Number(
                                    event.dataTransfer.getData('application/x-edit-eats-step')
                                )

                                onMoveStep(
                                    sectionPath,
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
                        onMoveStep={onMoveStep}
                    />
                ))}
            </div>
        </>
    )
}

export default InstructionSection