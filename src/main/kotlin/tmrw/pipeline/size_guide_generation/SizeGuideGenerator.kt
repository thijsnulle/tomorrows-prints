package tmrw.pipeline.size_guide_generation

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.JpegWriter
import tmrw.model.Print
import tmrw.utils.Files
import tmrw.utils.Files.Companion.batchFolderWithoutExtension
import java.nio.file.Path
import kotlin.io.path.exists

const val SIZE_GUIDE_SIZE = 1024
const val SIZE_GUIDE_HEIGHT = 896
const val SIZE_GUIDE_WIDTH = 736

private data class Size(val x: Int, val y: Int, val w: Int, val h: Int)

class SizeGuideGenerator {

    private val writer = JpegWriter.compression(85).withProgressive(true)
    private val sizeGuideBackground = ImmutableImage.loader()
        .fromPath(Files.images.resolve("size-guide-template.png"))

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
        val output = Files.sizeGuides.batchFolderWithoutExtension(print)
            .let { it.resolveSibling("${it.fileName}.jpeg") }

        if (output.exists()) return output

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

        return sizeGuide.output(writer, output)
    }
}