package preview

import com.sksamuel.scrimage.ImmutableImage
import io.github.oshai.kotlinlogging.KotlinLogging
import theme.Theme
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.*

// TODO: Move `PosterJsonObject` and `Poster` data class to another file
// TODO: Rename `Poster` to `Print`
data class PosterJsonObject(
    val path: String,
    val prompt: String,
    val theme: String?,
    val previews: List<String>?,
    val thumbnail: String?,
    val printFileUrl: String?,
    val listingUrl: String?,
) {
    fun toPoster(): Poster = Poster(
        Paths.get("src/main/resources/images/posters").toAbsolutePath().resolve(path),
        prompt,
        if (theme == null) Theme.DEFAULT else Theme.valueOf(theme.uppercase()),
        previews?.map { preview -> Path(preview) } ?: emptyList(),
        Path(thumbnail ?: ""),
        printFileUrl ?: "",
        listingUrl ?: "",
    )
}

data class Poster(
    val path: Path,
    val prompt: String,
    val theme: Theme = Theme.DEFAULT,
    val previews: List<Path> = emptyList(),
    val thumbnail: Path = Paths.get(""),
    val printFileUrl: String = "",
    val listingUrl: String = "",
) {
    constructor(fileName: String, prompt: String): this(
        Paths.get("src/main/resources/images/posters").toAbsolutePath().resolve(fileName),
        prompt
    )
}
abstract class PreviewComposer {

    private val logger = KotlinLogging.logger {}

    fun composePreviewsFor(poster: Poster): Poster {
        logger.info { "Generating previews for ${poster.path.fileName}" }

        val directory = Paths.get("src/main/resources/images/previews")
            .resolve(poster.path.nameWithoutExtension)
            .toAbsolutePath()

        if (directory.exists()) {
            logger.info { "Previews for ${poster.path.fileName} already exist, returning existing previews." }

            return poster.copy(previews = directory.listDirectoryEntries("*.png"))
        }

        directory.createDirectory()

        return compose(poster)
    }

    abstract fun compose(poster: Poster): Poster
}
