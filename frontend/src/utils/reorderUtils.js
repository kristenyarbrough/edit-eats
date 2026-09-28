export function moveItem(items, fromIndex, toIndex) {
    if (
        fromIndex < 0 ||
        fromIndex >= items.length ||
        toIndex < 0 ||
        toIndex >= items.length
    ) {
        return items
    }

    const updatedItems = [...items]

    const [movedItem] = updatedItems.splice(fromIndex, 1)

    updatedItems.splice(toIndex, 0, movedItem)

    return updatedItems
}