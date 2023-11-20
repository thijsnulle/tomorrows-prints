package tmrw.pipeline.description_allocation

import tmrw.utils.Example
import tmrw.utils.Prompter

const val MAX_DESCRIPTION_LENGTH = 100

class DescriptionPrompter: Prompter<String>(
    prompt = """
        Your task is to generate, based on a set of keywords used to
        generate an image, a description for a social media post.

        The description should be less than $MAX_DESCRIPTION_LENGTH characters.
        The description should not describe anything happening in the poster.
        The description should include relevant keywords near the beginning of the description.
        The description should describe the print in adjectives, extracted from the prompt.
        The description should follow a natural sentence structure.
        The description should never contain an artist.
    """.trimIndent(),
    examples = listOf(
        Example(
            "a red and white cloth with geometric shapes, in the style of organic shapes and curved lines, dark orange and beige, chiaroscuro woodcuts, playful lines, tupinipunk, spontaneous mark-making, mind-bending patterns",
            "Description: A vibrant print of playful lines and mind-bending patterns by Tomorrow's Prints, inspired by chiaroscuro woodcuts."
        ),
        Example(
            "a white painting with green shapes on it, in the style of bold stencil, dark beige and white, bloomcore, organic shapes, letras y figuras, bold curves, bold color blobs",
            "Description: A striking print with bold, green shapes and curves, by Tomorrow's Prints."
        ),
        Example(
            "an image resembling a vintage-style poster for the song 'Colors,' using minimalist lines and incorporating elements of carpetpunk, chalk art, dramatic diagonals, and rainbowcore",
            "Description: A colorful and minimalist print by Tomorrow's Prints, inspired by carpetpunk and chalk art, with dramatic diagonals."
        ),
        Example(
            "geometric posters by jack edouard, in the style of dark white and orange, mid-century modern design, grid, jessie arms botke, rounded shapes, balance, dark orange and black",
            "Description: A stunning print with rounded shapes and a grid design by Tomorrow's Prints, inspired by mid-century modern design."
        ),
        Example(
            "claude monet's sunflower garden, in the style of child-like innocence, grandiose ruins, transcendental art, low resolution, decorative paintings, outdoor scenes, quadratura",
            "Description: A transcendent print depicting child-like innocence with decorative flourishes by Tomorrow's Prints."
        )
    )
) {


    override fun process(output: String): String {
        val result = Regex("""Description: (.+)""").find(output)

        require (result != null) { "Generated Pinterest content should follow the correct structure:\n\n$output" }

        return result.groupValues[1]
    }
}