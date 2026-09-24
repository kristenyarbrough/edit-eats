export function formatTime(totalMinutes) {
    if (totalMinutes == null) {
        return ''
    }

    const hours = Math.floor(totalMinutes / 60)
    const minutes = totalMinutes % 60

    if (hours === 0) {
        return `${minutes} mins`
    }

    if (minutes === 0) {
        return `${hours}${hours === 1 ? ' hr' : ' hrs'}`
    }

    return `${hours}${hours === 1 ? ' hr ' : ' hrs '}${minutes} mins`
}