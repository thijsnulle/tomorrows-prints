package utility.transformation

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import java.nio.file.Path

val MAX_PIXELS_PER_SIDE = 10800

class ImageTransformer(val upscaler: ImageUpscaler) {
    fun upscale(input: Path, output: Path, target: Int = MAX_PIXELS_PER_SIDE): Path {
        val image = ImmutableImage.loader().fromPath(input)
        val imageIsLargerThanTarget = image.width > target || image.height > target

        return when(imageIsLargerThanTarget) {
            true -> downscale(image, output, target)
            false -> upscale(upscaler.upscale(input, output), output, target)
        }
    }

    private fun downscale(image: ImmutableImage, output: Path, target: Int): Path {
        return when(image.width > image.height) {
            true -> image.scaleToWidth(target)
            false -> image.scaleToHeight(target)
        }.output(PngWriter(), output)
    }
}
