package utility.prompt

enum class InteriorStyle {
    BAUHAUS,
    MIDCENTURY_MODERN,
    MINIMALIST,
    NORDIC,
}

typealias InteriorStyles = List<InteriorStyle>

class TemplatePromptHandler: AbstractPromptHandler<InteriorStyles>(
    prompt = """
        Your task is to determine, based on a set of keywords used to 
        generate a poster image, the interior style that is best associated
        with the keywords from a list of pre-determined themes. The available
        interior styles are: ${InteriorStyle.entries.map { it.name.lowercase() }.joinToString(", ")}.
        These interior styles revolve around living spaces, and the poster
        descriptions should align with the selected interior style. Provide the
        top three interior styles in order of similarity based on the keywords provided.
    """.trimIndent(),
    examples = listOf(
        Example(
            "a red and black circle sticker with the fingerprints in it, in the style of modernist landscapes, poster art, undulating lines, minimalist portraits, screen printing, humanist approach, afro-colombian themes",
            "bauhaus, midcentury_modern, minimalist"
        ),
    )
) {
    // TODO: assert that all template styles have folder in `templates` folder on instantiation

    override fun process(output: String): InteriorStyles {
        val styles = output
            .split(", ")
            .map { InteriorStyle.valueOf(it.trim().uppercase()) }

        require(styles.size == 3) { "Expected exactly three styles" }

        return styles
    }
}