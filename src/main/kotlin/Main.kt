import preview.Poster
import preview.SimplePreviewComposer
import kotlin.io.path.Path

suspend fun main(args: Array<String>) {
    val poster = Poster(
        path = Path("/Users/thijsnulle/Documents/Git/tomorrows-prints/src/main/resources/images/posters/poster_square.png"),
        prompt = "prompt"
    )

    val previewComposer = SimplePreviewComposer()
    previewComposer.compose(poster)
}