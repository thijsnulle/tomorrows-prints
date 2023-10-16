package preview

import theme.Theme
import java.nio.file.Path
import java.nio.file.Paths

data class Poster(
    val path: Path,
    val prompt: String,
    val theme: Theme = Theme.DEFAULT,
    val previews: List<Path> = emptyList(),
    val thumbnail: Path = Paths.get(""),
) {
    constructor(fileName: String, prompt: String): this(
        Paths.get("src/main/resources/images/posters").toAbsolutePath().resolve(fileName),
        prompt
    )
}
interface PreviewComposer {
    fun compose(poster: Poster): Poster
}
