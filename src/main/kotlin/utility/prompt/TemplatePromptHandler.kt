package utility.prompt

enum class TemplateStyle {
    ABSTRACT,
    COLLAGE,
    ILLUSTRATED,
    MINIMALIST,
    RETRO,
    TYPOGRAPHIC,
    VINTAGE,
}

data class TemplateResponse(
    val first: TemplateStyle,
    val second: TemplateStyle,
    val third: TemplateStyle,
)

class TemplatePromptHandler: AbstractOpenAIPromptHandler<TemplateResponse>(
    prompt = """
        Your task is to determine, based on a set of keywords used to 
        generate a poster image, the theme that is best associated with 
        the keywords from a list of pre-determined themes. The available 
        themes are: ${TemplateStyle.entries.map { it.name.lowercase() }.joinToString(", ")}.
        These themes revolve around living spaces, 
        and the posters should align with the selected theme. Provide the 
        top three themes in order of similarity based on the keywords provided.
    """.trimIndent(),
    examples = listOf(
        Example("a red and black circle sticker with the fingerprints in it, in the style of modernist landscapes, poster art, undulating lines, minimalist portraits, screen printing, humanist approach, afro-colombian themes", "abstract, minimalist, retro"),
        Example("flower pot on an empty wooden table, in the style of crisp neo-pop illustrations, simplistic, teal sky, orange", "illustrated, minimalist, retro"),
        Example("the gas station in the desert, in the style of crisp neo-pop illustrations, minimalist portraits, neo-geo, sōsaku hanga, dark brown, teal and orange, everyday objects, 1970–present, simplistic", "minimalist, retro, vintage")
    )
) {
    // TODO: assert that all template styles have folder in `templates` folder on instantiation

    override fun process(output: String): TemplateResponse {
        val styles = output
            .split(", ")
            .map { TemplateStyle.valueOf(it.trim().uppercase()) }

        require(styles.size == 3) { "Expected exactly three styles" }

        return TemplateResponse(styles[0], styles[1], styles[2])
    }
}