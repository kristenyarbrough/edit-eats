export function updateNestedSection(
    sections,
    sectionPath,
    updateSection
) {
    const [index, ...remainingPath] = sectionPath

    return sections.map((section, currentIndex) => {
        if (currentIndex !== index) {
            return section
        }

        if (remainingPath.length === 0) {
            return updateSection(section)
        }

        return {
            ...section,
            sections: updateNestedSection(
                section.sections,
                remainingPath,
                updateSection
            )
        }
    })
}

export function removeNestedSection(
    sections,
    sectionPath
) {
    const [index, ...remainingPath] = sectionPath

    if (remainingPath.length === 0) {
        return sections.filter(
            (_, currentIndex) => currentIndex !== index
        )
    }

    return sections.map((section, currentIndex) => {
        if (currentIndex !== index) {
            return section
        }

        return {
            ...section,
            sections: removeNestedSection(
                section.sections,
                remainingPath
            )
        }
    })
}