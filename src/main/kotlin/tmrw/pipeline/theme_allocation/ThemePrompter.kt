package tmrw.pipeline.theme_allocation

import tmrw.utils.Prompter
import tmrw.utils.Example

enum class Theme(val value: String) {
    DEFAULT("Default"),
    ABSTRACT("Abstract"),
    ANIMAL("Animal"),
    COLOR_FIELD("Color Field"),
    CYBERPUNK("Cyberpunk"),
    IMPRESSIONISM("Impressionism"),
    MINIMALISM("Minimalism"),
    PIXEL_ART("Pixel Art"),
    POP_ART("Pop Art"),
    PSYCHEDELIC("Psychedelic"),
    RETRO("Retro"),
}

class ThemePrompter: Prompter<Theme>(
    prompt = """
        Your task is to determine the most fitting theme from a list, based on a set of keywords used to create a poster image.
        The available themes are: ${Theme.entries.filterNot { it == Theme.DEFAULT }.joinToString(", ") { it.value }}.
    """.trimIndent(),

    examples = listOf(
        Example("poster with two people walking on a platform, orange background, in the style of minimalistic surrealism, dark red and light aquamarine, contrast of scale, art that plays with scale, long lens, high horizon lines", "Minimalism"),
        Example("an abstract painting of green and white animallike patterns, in the style of gary hume, organic simplicity, dark white and dark green, shaped canvases, leaf patterns, henri matisse, dappled", "Abstract"),
        Example("paris on a rainy night, 1900s paris, in the style of expressionist color palette, atmospheric urbanscapes, strip painting, light bronze and blue, plein-air, nightscapes", "Impressionism"),
        Example("bauhaus poster retro poster in orange, yellow and brown, in the style of calming symmetry, flowing lines, patrick brown, groovy, light beige and red, carpetpunk, wallpaper", "Retro"),
        Example("the gas station in the desert, in the style of crisp neo-pop illustrations, minimalist portraits, neo-geo, sōsaku hanga, dark brown, teal and orange, everyday objects, 1970–present, simplistic", "Pop Art"),
    )
) {
    override fun process(output: String): Theme = Theme.valueOf(output.trim().replace(' ', '_').uppercase())
}
