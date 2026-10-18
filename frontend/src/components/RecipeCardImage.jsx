import { useState } from 'react'

function RecipeCardImage({ imageUrl, recipeName }) {
    const [imageFailed, setImageFailed] = useState(false)

    if (!imageUrl || imageFailed) {
        return (
            <div
                className="recipe-card-image-placeholder"
                aria-hidden="true"
            >
                <img
                    src="/edit-eats-logo-without-name.svg"
                    alt=""
                    className="recipe-card-placeholder-logo"
                />
                <span className="recipe-card-placeholder-name">
                    EDIT EATS
                </span>
            </div>
        )
    }

    return (
        <img
            src={imageUrl}
            alt={recipeName}
            className="recipe-card-image"
            onError={() => setImageFailed(true)}
        />
    )
}

export default RecipeCardImage