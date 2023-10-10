package preview

import java.nio.file.Path
import java.nio.file.Paths

data class Poster(
    val path: Path,
    val prompt: String,
    val previews: List<Path> = emptyList(),
) {
    constructor(fileName: String, prompt: String): this(
        Paths.get("src/main/resources/images/posters").toAbsolutePath().resolve(fileName),
        prompt
    )
}
interface PreviewComposer {
    fun compose(poster: Poster): Poster
}
