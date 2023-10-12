package social.pinterest

import utility.prompt.AbstractPromptHandler
import utility.prompt.Example

enum class PinterestTheme(val value: String) {
    ABSTRACT("Abstract"),
    MINIMALIST("Minimalist"),
    VINTAGE("Vintage"),
}

class PinterestThemePromptHandler: AbstractPromptHandler<PinterestTheme>(
    prompt = """
        Your task is to determine, based on a set of keywords used to 
        generate a poster image, the theme that is best associated
        with the keywords from a list of pre-determined themes. The available
        themes are: ${PinterestTheme.entries.joinToString(", ") { it.name.lowercase() }}.
        Only return the theme that fits the description of the image the best.
    """.trimIndent(),

    examples = listOf(
        Example("a poster with two people walking from one side of a platform on an orange background, in the style of minimalistic surrealism, dark red and light aquamarine, contrast of scale, art that plays with scale, long lens, high horizon lines, raw versus finished", "Minimalist"),
        Example("an abstract painting of green and white animallike patterns, in the style of gary hume, organic simplicity, dark white and dark green, shaped canvases, leaf patterns, henri matisse, dappled", "Abstract"),
        Example("eight retro style cassette tapes in different colors, in the style of dark beige and violet, Alena Aenami, mechanical designs, creative commons attribution, Frank Quitely, industrial forms, neo-mosaic", "Vintage")
    )
) {
    override fun process(output: String): PinterestTheme = PinterestTheme.valueOf(output.trim().uppercase())
}