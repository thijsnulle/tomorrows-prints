package tmrw.pipeline.print_file_generation

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.math.max

const val MAX_PIXELS_PER_SIDE_PRINT = 10800
const val SCALE_FACTOR = 4

class ImageUpscaler(private val upscaler: ImageUpscalerImpl) {

    fun upscale(input: Path, maxPixelsPerSide: Int = MAX_PIXELS_PER_SIDE_PRINT, deleteInput: Boolean = false): Path {
        val image = ImmutableImage.loader().fromPath(input)
        val output = if (deleteInput) input else input.parent.resolve("upscaled/${input.name}")

        Files.createDirectories(output.parent)

        if (max(image.width, image.height) == maxPixelsPerSide) return input
        if (max(image.width, image.height) > maxPixelsPerSide) return downscale(image, output, maxPixelsPerSide)

        if (max(image.width, image.height) * SCALE_FACTOR > maxPixelsPerSide) {
            val downscaledImage = downscale(image, input, maxPixelsPerSide / SCALE_FACTOR)
            return upscale(downscaledImage, maxPixelsPerSide, true)
        }

        return when(output.exists() && !deleteInput) {
            true -> upscale(output, maxPixelsPerSide, true)
            false -> upscale(upscaler.upscale(input, output), maxPixelsPerSide, true)
        }
    }

    private fun downscale(image: ImmutableImage, output: Path, maxPixelsPerSide: Int): Path {
        KotlinLogging.logger {}.info { "Downscaling ${output.fileName} to $maxPixelsPerSide pixels" }

        return when(image.width > image.height) {
            true -> image.scaleToWidth(maxPixelsPerSide)
            false -> image.scaleToHeight(maxPixelsPerSide)
        }.output(PngWriter(), output)
    }
}
