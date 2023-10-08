package utility.transformation

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

const val MAX_PIXELS_PER_SIDE = 10800

class ImageUpscaler(private val upscaler: ImageUpscalerImpl) {

    fun upscale(input: Path, output: Path, target: Int = MAX_PIXELS_PER_SIDE): Path {
        val image = ImmutableImage.loader().fromPath(input)
        val imageIsLargerThanTarget = image.width > target || image.height > target

        return when(imageIsLargerThanTarget) {
            true -> downscale(image, output, target)
            false -> upscale(upscaler.upscale(input, output), output, target)
        }
    }

    fun upscaleFolder(inputFolder: Path, outputFolder: Path, target: Int = MAX_PIXELS_PER_SIDE): List<Path> {
        return inputFolder
            .listDirectoryEntries("*.png")
            .map { input -> upscale(input, outputFolder.resolve(input.name), target) }
    }

    private fun downscale(image: ImmutableImage, output: Path, target: Int): Path {
        return when(image.width > image.height) {
            true -> image.scaleToWidth(target)
            false -> image.scaleToHeight(target)
        }.output(PngWriter(), output)
    }
}
