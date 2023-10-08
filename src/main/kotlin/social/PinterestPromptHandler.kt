package social

import utility.prompt.AbstractPromptHandler
import utility.prompt.Example

data class PinterestContent(
    val title: String,
    val description: String,
    val altText: String,
)

class PinterestPromptHandler: AbstractPromptHandler<PinterestContent>(
    // TODO: remove descriptions of the poster from the description
    prompt = """
        Your task is to generate, based on a set of keywords used to
        generate a poster image, a title, description, and alt text for
        a social media post. The title should be minimum 60-80 characters
        long, it should contain the word poster, and it should contain a
        description of the style. The description should be less than 250 characters.
        The description should not describe anything happening in the poster.
        The alt text should describe the poster's content in maximum 100 characters.
    """.trimIndent(),
    examples = listOf(
        Example(
            "a red and white cloth with geometric shapes, in the style of organic shapes and curved lines, dark orange and beige, chiaroscuro woodcuts, playful lines, tupinipunk, spontaneous mark-making, mind-bending patterns",
            "Title: Organic Shapes and Mind-Bending Patterns - A Bold and Vibrant Poster\nDescription: This poster boasts a unique mix of organic shapes and curved lines, combined with playful lines and mind-bending patterns. The dark orange and beige colors add depth to the chiaroscuro woodcut style, making it a visually stunning piece.\nAlt text: Vibrant poster with a mix of organic shapes and mind-bending patterns in shades of orange and beige"
        ),
        Example(
            "a white painting with green shapes on it, in the style of bold stencil, dark beige and white, bloomcore, organic shapes, letras y figuras, bold curves, bold color blobs",
            "Title: Bold Stencil and Organic Shapes - A Dynamic and Minimalist Poster\nDescription: This minimalist poster features bold stencil-style designs and organic shapes, enhanced by bold curves and color blobs. The dark beige and white color palette adds a touch of sophistication and bloomcore aesthetic to the overall design.\nAlt text: Dynamic and minimalist poster with bold stencil designs, organic shapes, and pops of beige and white"
        )
    )
) {

    // Embrace the organic shapes and bold curves of this stunning stencil poster, featuring dark beige and white bloomcore designs. Letras y figuras add an element of sophistication, while bold color blobs make a statement.
    override fun process(output: String): PinterestContent {
        val result = Regex("""
            Title: (.*)
            Description: (.*)
            Alt text: (.*)
        """.trimIndent()).find(output)

        require(result != null) { "Generated Pinterest content should follow the correct structure" }

        return PinterestContent(result.groupValues[1], result.groupValues[2], result.groupValues[3])
    }
}