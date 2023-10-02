package preview

import java.nio.file.Path

data class Poster(
    val path: Path,
    val prompt: String,
    val previews: List<Path> = emptyList(),
)
interface PreviewComposer {
    suspend fun compose(poster: Poster): Poster
}
