package utility.transformation

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.ImmutableImageLoader
import com.sksamuel.scrimage.nio.PngWriter
import model.Print
import utility.files.Files
import utility.files.Files.Companion.batchFolder
import java.awt.Color
import java.nio.file.Path
import kotlin.io.path.exists

const val SIZE: Int = 900
const val INNER_MARGIN: Int = 5
const val OUTER_MARGIN: Int = 100
const val TOTAL_MARGIN: Int = INNER_MARGIN + OUTER_MARGIN

class ThumbnailGenerator {

    private val backgroundColor = Color.decode("#A7C7E7")

    fun generateThumbnail(print: Print): Path {
        val output = Files.thumbnails.batchFolder(print)

        if (output.exists()) return output

        val image = ImmutableImageLoader().fromPath(print.path)

        val width = if (image.width > image.height) SIZE else SIZE * image.width / image.height
        val height = if (image.height > image.width) SIZE else SIZE * image.height / image.width

        val x = (SIZE - width) / 2 + TOTAL_MARGIN
        val y = (SIZE - height) / 2 + TOTAL_MARGIN

        val background = ImmutableImage.create(SIZE + 2 * TOTAL_MARGIN, SIZE + 2 * TOTAL_MARGIN).map { backgroundColor }
        val border = ImmutableImage.create(width + 2 * INNER_MARGIN, height + 2 * INNER_MARGIN).map { Color.WHITE }

        return background
            .overlay(border, x - INNER_MARGIN, y - INNER_MARGIN)
            .overlay(image.cover(width, height), x, y)
            .output(PngWriter(), output)
    }
}