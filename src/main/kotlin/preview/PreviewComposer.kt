package preview

import theme.Theme
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path

data class PosterJsonObject(
    val path: String,
    val prompt: String,
    val theme: String?,
    val previews: List<String>?,
    val thumbnail: String?,
    val printFileUrl: String?
) {
    fun toPoster(): Poster = Poster(
        Paths.get("src/main/resources/images/posters").toAbsolutePath().resolve(path),
        prompt,
        if (theme == null) Theme.DEFAULT else Theme.valueOf(theme.uppercase()),
        previews?.map { preview -> Path(preview) } ?: emptyList(),
        Path(thumbnail ?: ""),
        printFileUrl ?: ""
    )
}

data class Poster(
    val path: Path,
    val prompt: String,
    val theme: Theme = Theme.DEFAULT,
    val previews: List<Path> = emptyList(),
    val thumbnail: Path = Paths.get(""),
    val printFileUrl: String = ""
) {
    constructor(fileName: String, prompt: String): this(
        Paths.get("src/main/resources/images/posters").toAbsolutePath().resolve(fileName),
        prompt
    )
}
interface PreviewComposer {
    fun compose(poster: Poster): Poster
}
