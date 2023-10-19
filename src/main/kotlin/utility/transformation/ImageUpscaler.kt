package utility.transformation

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.name

const val MAX_PIXELS_PER_SIDE_POSTER = 10800

class ImageUpscaler(private val upscaler: ImageUpscalerImpl) {

    fun upscale(input: Path, maxPixelsPerSide: Int = MAX_PIXELS_PER_SIDE_POSTER, deleteInput: Boolean = false): Path {
        val image = ImmutableImage.loader().fromPath(input)
        val output = if (deleteInput) input else input.parent.resolve("upscaled/${input.name}")
        val imageIsLargerThanTarget = image.width >= maxPixelsPerSide || image.height >= maxPixelsPerSide

        if (imageIsLargerThanTarget) {
            return downscale(image, output, maxPixelsPerSide)
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
