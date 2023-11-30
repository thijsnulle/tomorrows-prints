package tmrw.pipeline.theme_allocation

import tmrw.utils.Prompter
import tmrw.utils.Example

enum class Theme(val value: String) {
    DEFAULT("Default"),
    ABSTRACT("Abstract"),
    COLOR_FIELD("Color Field"),
    CUBISM("Cubism"),
    EXPRESSIONISM("Expressionism"),
    IMPRESSIONISM("Impressionism"),
    MINIMALISM("Minimalism"),
    POP_ART("Pop Art"),
    RETRO("Retro"),
    ROMANTICISM("Romanticism"),
    SURREALISM("Surrealism"),
}

class ThemePrompter: Prompter<Theme>(
    prompt = """
        Your task is to determine, based on a set of keywords used to 
        generate a poster image, the theme that is best associated
        with the keywords from a list of pre-determined themes. The available
        themes are: ${Theme.entries.filterNot { it == Theme.DEFAULT }.joinToString(", ") { it.value }}.
        Only return the theme that fits the description of the image the best.
    """.trimIndent(),

    examples = listOf(
        Example("a poster with two people walking from one side of a platform on an orange background, in the style of minimalistic surrealism, dark red and light aquamarine, contrast of scale, art that plays with scale, long lens, high horizon lines, raw versus finished", "Minimalism"),
        Example("an abstract painting of green and white animallike patterns, in the style of gary hume, organic simplicity, dark white and dark green, shaped canvases, leaf patterns, henri matisse, dappled", "Abstract"),
        Example("mountain range with blue skies and layered clouds, abstract, expressionistic colour pallete", "Expressionism"),
        Example("bauhaus poster retro poster in orange, yellow and brown, in the style of calming symmetry, flowing lines, patrick brown, groovy, light beige and red, carpetpunk, wallpaper --ar 4:5", "Retro"),
        Example("the gas station in the desert, in the style of crisp neo-pop illustrations, minimalist portraits, neo-geo, sōsaku hanga, dark brown, teal and orange, everyday objects, 1970–present, simplistic --ar 2:3", "Pop art"),
    )
) {
    override fun process(output: String): Theme = Theme.valueOf(output.trim().replace(' ', '_').uppercase())
}