package social.pinterest

import utility.prompt.Example
import utility.prompt.OpenAIPrompter

data class PinterestContent(
    val title: String,
    val description: String,
    val altText: String,
)

const val MAX_TITLE_LENGTH = 60
const val MAX_DESCRIPTION_LENGTH = 100
const val MAX_ALT_TEXT_LENGTH = 100
const val MAX_NUMBER_OF_HASHTAGS = 3

class PinterestContentPrompter: OpenAIPrompter<PinterestContent>(
    prompt = """
        Your task is to generate, based on a set of keywords used to
        generate an image, a title, description, and alt text for
        a social media post.

        The title should be less than $MAX_TITLE_LENGTH characters long.
        The title should end with "• Tomorrow's Prints".
        The title should include important keywords of the prompt.
        The title should be in headline case.

        The description should be less than $MAX_DESCRIPTION_LENGTH characters.
        The description should not describe anything happening in the poster.
        The description should include relevant keywords near the beginning of the description.
        The description should describe the print in adjectives, extracted from the prompt.
        The description should follow a natural sentence structure.
        The description should never contain an artist.
        The description should end with a call to action on where to buy it.

        The alt text should be less than $MAX_ALT_TEXT_LENGTH characters.
        The alt text should only describe the style of the poster.
        The alt text should never contain an artist.
        The alt text should not contain "Tomorrow's Prints"
    """.trimIndent(),
    examples = listOf(
        Example(
            "a red and white cloth with geometric shapes, in the style of organic shapes and curved lines, dark orange and beige, chiaroscuro woodcuts, playful lines, tupinipunk, spontaneous mark-making, mind-bending patterns",
            "Title: Geometry in Motion • Tomorrow's Prints\nDescription: A vibrant print of playful lines and mind-bending patterns by Tomorrow's Prints, inspired by chiaroscuro woodcuts. Get yours at [link]!\nAlt text: Red and white cloth with geometric shapes and playful lines inspired by woodcuts, tupinipunk and spontaneous mark-making."
        ),
        Example(
            "a white painting with green shapes on it, in the style of bold stencil, dark beige and white, bloomcore, organic shapes, letras y figuras, bold curves, bold color blobs",
            "Title: Bold and Green • Tomorrow's Prints\nDescription: A striking print with bold, green shapes and curves, by Tomorrow's Prints. Shop now at [link]!\nAlt text: White painting with bold green shapes and organic curves in stencil and letras y figuras style."
        ),
        Example(
            "an image resembling a vintage-style poster for the song 'Colors,' using minimalist lines and incorporating elements of carpetpunk, chalk art, dramatic diagonals, and rainbowcore",
            "Title: Bringing Colors to Life • Tomorrow's Prints\nDescription: A colorful and minimalist print by Tomorrow's Prints, inspired by carpetpunk and chalk art, with dramatic diagonals. See the print at [link]!\nAlt text: Vintage-style poster with carpetpunk, chalk art, diagonals, and rainbowcore elements."
        ),
        Example(
            "geometric posters by jack edouard, in the style of dark white and orange, mid-century modern design, grid, jessie arms botke, rounded shapes, balance, dark orange and black",
            "Title: Form and Function • Tomorrow's Prints\nDescription: A stunning print with rounded shapes and a grid design by Tomorrow's Prints, inspired by mid-century modern design. Take a look at [link]!\nAlt text: Geometric posters with grid design and rounded shapes, inspired by mid-century modern art."
        ),
        Example(
            "claude monet's sunflower garden, in the style of child-like innocence, grandiose ruins, transcendental art, low resolution, decorative paintings, outdoor scenes, quadratura",
            "Title: Garden of Remembrance • Tomorrow's Prints\nDescription: A transcendent print depicting child-like innocence with decorative flourishes by Tomorrow's Prints. Get your version at [link]!\nAlt text: Sunflower garden with a whimsical painting style, inspired by Claude Monet."
        )
    )
) {
    companion object {
        private val hashtags = listOf(
            "aesthetic",
            "art",
            "artinspiration",
            "artist",
            "artwork",
            "decor",
            "decoratingideas",
            "design",
            "designideas",
            "gift",
            "gifts",
            "giftsforher",
            "giftsforhim",
            "giftideas",
            "homeinspiration",
            "interiordesign",
            "interiordesire",
            "interiorlovers",
            "interiors",
            "interiorstylist",
            "lifestyle",
            "modernhome",
            "pinterestart",
            "pinterestideas",
            "pinterestinspired",
            "walldecor",
        )
    }

    override fun process(output: String): PinterestContent {
        val result = Regex("""
            Title: (.*)
            Description: ([^.]+\.[^\[]+\[link]!)
            Alt text: (.*)
        """.trimIndent()).find(output)

        require (result != null) { "Generated Pinterest content should follow the correct structure:\n\n$output" }

        val (_, title, descriptionWithoutHashtags, altText) = result.groupValues

        val hashtags = hashtags.shuffled().take(MAX_NUMBER_OF_HASHTAGS).joinToString(" "){ "#$it" }
        val description = "$descriptionWithoutHashtags $hashtags"

        return PinterestContent(title, description, altText)
    }
}