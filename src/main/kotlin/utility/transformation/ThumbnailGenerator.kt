package utility.transformation

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.ImmutableImageLoader
import com.sksamuel.scrimage.nio.PngWriter
import preview.Poster
import java.awt.Color
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name
import kotlin.math.abs

const val SIZE: Int = 900
const val INNER_MARGIN: Int = 5
const val OUTER_MARGIN: Int = 50
const val TOTAL_MARGIN: Int = INNER_MARGIN + OUTER_MARGIN

class ThumbnailGenerator {

    private val backgroundColors = listOf(
        "#FF8080", "#FD8A8A", "#FFCBCB", "#FFCF96", "#F6FDC3", "#CDFAD5", "#A8D1D1", "#9EA1D4",
    ).map { Color.decode(it) }

    fun generateThumbnail(poster: Poster): Path {
        val print = ImmutableImageLoader().fromPath(poster.path)

        val width = if (print.width > print.height) SIZE else SIZE * print.width / print.height
        val height = if (print.height > print.width) SIZE else SIZE * print.height / print.width

        val x = (SIZE - width) / 2 + TOTAL_MARGIN
        val y = (SIZE - height) / 2 + TOTAL_MARGIN

        val backgroundColor = findBackgroundColor(print)
        val background = ImmutableImage.create(SIZE + 2 * TOTAL_MARGIN, SIZE + 2 * TOTAL_MARGIN).map { backgroundColor }
        val border = ImmutableImage.create(width + 2 * INNER_MARGIN, height + 2 * INNER_MARGIN).map { Color.WHITE }

        return background
            .overlay(border, x - INNER_MARGIN, y - INNER_MARGIN)
            .overlay(print.cover(width, height), x, y)
            .output(
                PngWriter(),
                Paths.get("src/main/resources/images/thumbnails/${poster.path.name}").toAbsolutePath()
            )
    }

    private fun findBackgroundColor(image: ImmutableImage): Color {
        val pixels = image.pixels()

        val averageR = pixels.sumOf { it.red() } / pixels.size
        val averageG = pixels.sumOf { it.green() } / pixels.size
        val averageB = pixels.sumOf { it.blue() } / pixels.size

        return backgroundColors.minBy {
            abs(it.red - averageR) + abs(it.green - averageG) + abs(it.blue - averageB)
        }
    }
}