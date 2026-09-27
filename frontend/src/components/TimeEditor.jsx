function TimeEditor({ label, value, onChange }) {
    const hours = value == null || value === ''
        ? ''
        : Math.floor(value / 60)

    const minutes = value == null || value === ''
        ? ''
        : value % 60

    const updateTime = (newHours, newMinutes) => {
        if (!onChange) {
            return
        }

        const hoursValue = newHours === '' ? 0 : Number(newHours)
        const minutesValue = newMinutes === '' ? 0 : Number(newMinutes)

        onChange((hoursValue * 60) + minutesValue)
    }

    return (
        <label className="recipe-meta-field">
            {label}

            <input
                type="Number"
                min="0"
                value={hours}
                disabled={!onChange}
                onChange={(event) =>
                    updateTime(
                        event.target.value,
                        minutes
                    )
                }
            />
            hrs

            <input
                type="number"
                min="0"
                max="59"
                value={minutes}
                disabled={!onChange}
                onChange={(event) =>
                    updateTime(
                        hours,
                        event.target.value
                    )
                }
            />
            mins
        </label>
    )
}

export default TimeEditor