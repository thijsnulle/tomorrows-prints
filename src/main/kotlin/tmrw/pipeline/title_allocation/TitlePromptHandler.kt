package tmrw.pipeline.title_allocation

import tmrw.utils.Example
import tmrw.utils.Prompter

const val MAX_TITLE_LENGTH = 60

class TitlePromptHandler: Prompter<String>(
    prompt = """
        Your task is to generate, based on a set of keywords used to
        generate an image, a title for a listing of a poster.

        The title should be less than $MAX_TITLE_LENGTH characters long.
        The title should end with "• Tomorrow's Prints".
        The title should include important keywords of the prompt.
        The title should be in headline case.
    """.trimIndent(),
    examples = listOf(
        Example(
            "a red and white cloth with geometric shapes, in the style of organic shapes and curved lines, dark orange and beige, chiaroscuro woodcuts, playful lines, tupinipunk, spontaneous mark-making, mind-bending patterns",
            "Title: Geometry in Motion • Tomorrow's Prints"
        ),
        Example(
            "a white painting with green shapes on it, in the style of bold stencil, dark beige and white, bloomcore, organic shapes, letras y figuras, bold curves, bold color blobs",
            "Title: Bold and Green • Tomorrow's Prints"
        ),
        Example(
            "an image resembling a vintage-style poster for the song 'Colors,' using minimalist lines and incorporating elements of carpetpunk, chalk art, dramatic diagonals, and rainbowcore",
            "Title: Bringing Colors to Life • Tomorrow's Prints"
        ),
        Example(
            "geometric posters by jack edouard, in the style of dark white and orange, mid-century modern design, grid, jessie arms botke, rounded shapes, balance, dark orange and black",
            "Title: Form and Function • Tomorrow's Prints"
        ),
        Example(
            "claude monet's sunflower garden, in the style of child-like innocence, grandiose ruins, transcendental art, low resolution, decorative paintings, outdoor scenes, quadratura",
            "Title: Garden of Remembrance • Tomorrow's Prints"
        )
    )
) {
    override fun process(output: String): String {
        val result = Regex("""Title: (.* • Tomorrow's Prints)""").find(output)

        require (result != null) { "Generated title should follow the correct structure:\n\n$output" }

        val (_, title) = result.groupValues

        return title
    }
}