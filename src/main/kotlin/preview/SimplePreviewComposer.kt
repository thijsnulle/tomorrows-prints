package preview

import com.google.common.math.IntMath.pow
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import io.github.oshai.kotlinlogging.KotlinLogging
import java.awt.Color
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension
import kotlin.math.abs

const val SIZE = 2048
const val POSTER_WIDTH = 768
const val POSTER_HEIGHT = 1152
const val POSTER_X = (SIZE - POSTER_WIDTH) / 2
const val POSTER_Y = (SIZE - POSTER_HEIGHT) / 2
const val GRAY_IMAGE_THRESHOLD = 5

// TODO: add support for horizontal posters
class SimplePreviewComposer : PreviewComposer {

    private val loader = ImmutableImage.loader()
    private val logger = KotlinLogging.logger {}

    private val colors = listOf(
        Color(255, 240, 240),
        Color(240, 255, 240),
        Color(240, 240, 255),
        Color(255, 255, 240),
        Color(255, 240, 255),
        Color(240, 255, 255)
    )

    private val frames = Paths.get("src/main/resources/images/frames").toAbsolutePath()
        .listDirectoryEntries("*.png").map { loader.fromPath(it) }

    override fun compose(poster: Poster): Poster {
        logger.info { "Generating previews for ${poster.path.fileName}" }

        val directory = Paths.get("src/main/resources/images/previews")
            .resolve(poster.path.nameWithoutExtension)
            .toAbsolutePath()

        if (directory.exists()) {
            logger.info { "Previews for ${poster.path.fileName} already exist, returning existing previews." }

            return poster.copy(previews = directory.listDirectoryEntries("*.png"))
        }

        directory.createDirectory()

        val posterImage = loader.fromPath(poster.path).cover(POSTER_WIDTH, POSTER_HEIGHT)
        val outputFolder = Paths.get("src/main/resources/images/previews/${poster.path.nameWithoutExtension}").toAbsolutePath()

        val backgroundColor = backgroundColorFor(posterImage)
        val background = ImmutableImage.filled(SIZE, SIZE, backgroundColor)

        val previews = frames.map { frame -> background.overlay(posterImage, POSTER_X, POSTER_Y).overlay(frame) }
            .map { it.output(PngWriter(), outputFolder.resolve("${UUID.randomUUID()}.png")) }

        return poster.copy(previews = previews)
    }

    private fun backgroundColorFor(image: ImmutableImage): Color {
        val red = image.pixels().map { it.red() }.average().toInt()
        val green = image.pixels().map { it.green() }.average().toInt()
        val blue = image.pixels().map { it.blue() }.average().toInt()

        if (isGray(red, green, blue)) return Color.WHITE

        val averageColor = Color(red, green, blue)
        return colors.minBy { differenceBetweenColors(it, averageColor) }
    }

    private fun isGray(red: Int, green: Int, blue: Int): Boolean {
        return abs(red - green) <= GRAY_IMAGE_THRESHOLD &&
                abs(green - blue) <= GRAY_IMAGE_THRESHOLD &&
                abs(blue - red) <= GRAY_IMAGE_THRESHOLD
    }

    private fun differenceBetweenColors(c1: Color, c2: Color): Int {
        return pow(c1.red - c2.red, 2) +
                pow(c1.green - c2.green, 2) +
                pow(c1.blue - c2.blue, 2)
    }
}

