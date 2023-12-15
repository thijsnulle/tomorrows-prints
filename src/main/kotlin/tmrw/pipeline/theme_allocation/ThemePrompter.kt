package tmrw.pipeline.theme_allocation

import tmrw.utils.Prompter
import tmrw.utils.Example

enum class Theme(val value: String) {
    DEFAULT("Default"),
    ABSTRACT("Abstract"),
    ANIMAL("Animal"),
    DIGITAL_ART("Digital Art"),
    FANTASY("Fantasy"),
    IMPRESSIONISM("Impressionism"),
    MINIMALISM("Minimalism"),
    PSYCHEDELIC("Psychedelic"),
    RETRO("Retro"),
    SPACE("Space"),
}

class ThemePrompter: Prompter<Theme>(
    prompt = """
        Your task is to determine the most fitting theme from a list, based on a set of keywords used to create a poster image.
        The available themes are: ${Theme.entries.filterNot { it == Theme.DEFAULT }.joinToString(", ") { it.value }}.
    """.trimIndent(),

    examples = listOf(
        Example("two people walking, orange background, minimalistic surrealism, contrast of scale, art that plays with scale, long lens, high horizon lines", "Minimalism"),
        Example("abstract painting of green and white animallike patterns, organic simplicity, shaped canvases, leaf patterns", "Abstract"),
        Example("paris on a rainy night, in the style of expressionist color palette, atmospheric urbanscapes, plein-air", "Impressionism"),
        Example("bauhaus poster retro poster in orange, yellow and brown, in the style of calming symmetry, flowing lines", "Retro"),
        Example("the gas station in the desert, in the style of crisp neo-pop illustrations, minimalist portraits", "Pop Art"),
        Example("poster combining earths and suns stars, in the style of crisp neo-pop illustrations", "Space")
    )
) {
    override fun process(output: String): Theme = Theme.valueOf(output.trim().replace(' ', '_').uppercase())
}
