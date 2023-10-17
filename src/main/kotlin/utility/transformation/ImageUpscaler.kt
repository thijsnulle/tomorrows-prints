package utility.transformation

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension

const val MAX_PIXELS_PER_SIDE_POSTER = 10800

class ImageUpscaler(private val upscaler: ImageUpscalerImpl) {
    fun upscale(input: Path, maxPixelsPerSide: Int = MAX_PIXELS_PER_SIDE_POSTER, deleteInput: Boolean = false): Path {
        val image = ImmutableImage.loader().fromPath(input)
        val imageIsLargerThanTarget = image.width > maxPixelsPerSide || image.height > maxPixelsPerSide

        val output = when(deleteInput) {
            true -> input
            false -> input.parent.resolve("${input.nameWithoutExtension}-upscaled.png")
        }

        return when(imageIsLargerThanTarget) {
            true -> downscale(image, output, maxPixelsPerSide)
            false -> upscale(upscaler.upscale(input, output), maxPixelsPerSide, true)
        }
    }

    private fun downscale(image: ImmutableImage, output: Path, maxPixelsPerSide: Int): Path {
        return when(image.width > image.height) {
            true -> image.scaleToWidth(maxPixelsPerSide)
            false -> image.scaleToHeight(maxPixelsPerSide)
        }.output(PngWriter(), output)
    }
}
