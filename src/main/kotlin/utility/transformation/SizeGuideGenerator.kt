package utility.transformation

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import model.Print
import utility.files.Files
import java.awt.Color
import java.nio.file.Path
import java.util.UUID

const val SIZE_GUIDE_SIZE = 1024
const val SIZE_GUIDE_HEIGHT = 896
const val SIZE_GUIDE_WIDTH = 736

data class Size(val x: Int, val y: Int, val w: Int, val h: Int)

class SizeGuideGenerator {

    private val sizeGuideBackground = ImmutableImage.filled(
        SIZE_GUIDE_SIZE, SIZE_GUIDE_SIZE, Color.WHITE
    )

    private val sizes = listOf(
        Size(0, 0, 24, 36),
        Size(0, 40, 12, 16),
        Size(14, 38, 12, 18),
        Size(26, 0, 20, 30),
        Size(28, 32, 18, 24),
    )

    private val maxUnitsVertical = sizes.maxOf { it.y + it.h }
    private val maxUnitsHorizontal = sizes.maxOf { it.x + it.w }

    fun generateSizeGuide(print: Print): Path {
        val printImage = ImmutableImage.loader().fromPath(print.path)
        val offsetX = (SIZE_GUIDE_SIZE - SIZE_GUIDE_WIDTH) / 2
        val offsetY = (SIZE_GUIDE_SIZE - SIZE_GUIDE_HEIGHT) / 2

        val sizeGuide = sizes.fold(sizeGuideBackground) { guide, size ->
            val x = offsetX + (size.x * SIZE_GUIDE_WIDTH / maxUnitsHorizontal)
            val y = offsetY + (size.y * SIZE_GUIDE_HEIGHT / maxUnitsVertical)
            val w = (size.w * SIZE_GUIDE_WIDTH / maxUnitsHorizontal)
            val h = (size.h * SIZE_GUIDE_HEIGHT / maxUnitsVertical)

            guide.overlay(printImage.cover(w, h), x, y)
        }

        sizeGuide.output(PngWriter(), Files.sizeGuides.resolve("${UUID.randomUUID()}.png"))

        return Files.images.resolve("test.png")
    }
}