package social.pinterest

import utility.prompt.AbstractPromptHandler
import utility.prompt.Example

data class PinterestContent(
    val title: String,
    val description: String,
    val altText: String,
)

const val MAX_TITLE_LENGTH = 60
const val MAX_DESCRIPTION_LENGTH = 250
const val MAX_ALT_TEXT_LENGTH = 100
const val PINTEREST_DESCRIPTION_LENGTH: Int = 500

class PinterestPromptHandler : AbstractPromptHandler<PinterestContent>(
    prompt = """
        Your task is to generate, based on a set of keywords used to
        generate a poster image, a title, description, and alt text for
        a social media post.

        The title should be less than $MAX_TITLE_LENGTH characters long.
        The title should end with "- Tomorrow's Prints".
        The title should include important keywords of the prompt.
        The title should be in headline case.

        The description should be less than $MAX_DESCRIPTION_LENGTH characters.
        The description should not describe anything happening in the poster.
        The description should include relevant keywords near the beginning of the description.
        The description should have the brand "Tomorrow's Prints" in the first line of the description.
        The description should follow a natural sentence structure.
        The description should never contain an artist.

        The alt text should be less than $MAX_ALT_TEXT_LENGTH characters.
        The alt text should only describe the style of the poster.
    """.trimIndent(),
    examples = listOf(
        Example(
            "a red and white cloth with geometric shapes, in the style of organic shapes and curved lines, dark orange and beige, chiaroscuro woodcuts, playful lines, tupinipunk, spontaneous mark-making, mind-bending patterns",
            "Title: Organically Geometric Print - Tomorrow's Prints\nTomorrow's Prints brings you a vibrant red and white cloth with geometric shapes and playful lines. Experience the dynamic mix of organic and curved lines, chiaroscuro woodcuts, and mind-bending patterns with a touch of tupinipunk.\nAlt text: Red and white cloth with geometric shapes and playful lines inspired by woodcuts, tupinipunk and spontaneous mark-making"
        ),
        Example(
            "a white painting with green shapes on it, in the style of bold stencil, dark beige and white, bloomcore, organic shapes, letras y figuras, bold curves, bold color blobs",
            "Title: Bold and Green - Tomorrow's Prints\nDescription: Tomorrow's Prints showcases a striking white painting adorned with bold green shapes and curves, inspired by stencil and letras y figuras. Embrace the unique blend of bloomcore and organic shapes with bold color blobs.\nAlt text: White painting with bold green shapes and organic curves in stencil and letras y figuras style."
        ),
        Example(
            "an image resembling a vintage-style poster for the song 'Colors,' using minimalist lines and incorporating elements of carpetpunk, chalk art, dramatic diagonals, and rainbowcore",
            "Title: Bringing Colors to Life\nDescription: Tomorrow's Prints presents a vintage-inspired poster featuring carpetpunk and chalk art. The dramatic diagonals and vibrant rainbowcore make it a must-have for any art lover.\nAlt text: Vintage-style poster with carpetpunk, chalk art, diagonals, and rainbowcore elements."
        ),
        Example(
            "geometric posters by jack edouard, in the style of dark white and orange, mid-century modern design, grid, jessie arms botke, rounded shapes, balance, dark orange and black",
            "Title: Form and Function - Tomorrow's Prints\nDescription: Discover Tomorrow's Prints' collection of geometric posters with influences from mid-century modern design. Featuring a balance of rounded shapes and a grid structure in dark orange and black.\n"
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
            Description: (.*)
            Alt text: (.*)
        """.trimIndent()).find(output)

        // TODO: this still occasionally fails
        require(result != null) { "Generated Pinterest content should follow the correct structure" }

        val (_, title, descriptionWithoutHashtags, altText) = result.groupValues
        val description = hashtags.asSequence().shuffled()
            .fold(descriptionWithoutHashtags) { current, hashtag ->
                if ("$current #$hashtag".length > PINTEREST_DESCRIPTION_LENGTH) current else "$current #$hashtag"
            }

        return PinterestContent(title, description, altText)
    }
}