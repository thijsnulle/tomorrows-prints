package utility.transformation

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.ImmutableImageLoader
import com.sksamuel.scrimage.nio.PngWriter
import preview.Poster
import java.awt.Color
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name

const val SIZE: Int = 900
const val INNER_MARGIN: Int = 5
const val OUTER_MARGIN: Int = 100
const val TOTAL_MARGIN: Int = INNER_MARGIN + OUTER_MARGIN

class ThumbnailGenerator {

    private val backgroundColor = Color.decode("#A7C7E7")

    fun generateThumbnail(poster: Poster): Path {
        val print = ImmutableImageLoader().fromPath(poster.path)

        val width = if (print.width > print.height) SIZE else SIZE * print.width / print.height
        val height = if (print.height > print.width) SIZE else SIZE * print.height / print.width

        val x = (SIZE - width) / 2 + TOTAL_MARGIN
        val y = (SIZE - height) / 2 + TOTAL_MARGIN

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
}