package preview

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import java.awt.Color
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension

const val SIZE = 2048
const val POSTER_WIDTH = 768
const val POSTER_HEIGHT = 1152
const val POSTER_X = (SIZE - POSTER_WIDTH) / 2
const val POSTER_Y = (SIZE - POSTER_HEIGHT) / 2

// TODO: add support for horizontal posters
class SimplePreviewComposer : PreviewComposer() {

    private val loader = ImmutableImage.loader()

    private val backgrounds = listOf(
        Color(255, 255, 255),
        Color(255, 240, 240),
        Color(240, 240, 255),
    ).map { ImmutableImage.filled(SIZE, SIZE, it) }

    private val frames = Paths.get("src/main/resources/images/frames").toAbsolutePath()
        .listDirectoryEntries("*.png").map { loader.fromPath(it) }

    override fun compose(poster: Poster): Poster {
        val posterImage = loader.fromPath(poster.path).cover(POSTER_WIDTH, POSTER_HEIGHT)
        val outputFolder = Paths.get("src/main/resources/images/previews/${poster.path.nameWithoutExtension}").toAbsolutePath()

        val previews = backgrounds.flatMap { background ->
            frames.map { frame -> background
                .overlay(posterImage, POSTER_X, POSTER_Y)
                .overlay(frame)
                .output(PngWriter(), outputFolder.resolve("${UUID.randomUUID()}.png"))
            }
        }

        return poster.copy(previews = previews)
    }
}

