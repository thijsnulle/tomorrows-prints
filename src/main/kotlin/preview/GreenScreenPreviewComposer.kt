package preview

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import com.sksamuel.scrimage.pixels.Pixel
import io.github.oshai.kotlinlogging.KotlinLogging
import utility.transformation.ImageUpscaler
import utility.transformation.MAX_PIXELS_PER_SIDE_PREVIEW
import utility.transformation.upscaleWithRealESRGAN
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import kotlin.io.path.*

data class GreenScreen(val x: Int, val y: Int, val w: Int, val h: Int)

const val NUMBER_OF_TEMPLATES = 5

class GreenScreenPreviewComposer : PreviewComposer {

    private val logger = KotlinLogging.logger {}
    private val imageLoader = ImmutableImage.loader()
    private val images = Paths.get("src/main/resources/images").toAbsolutePath()

    override fun compose(poster: Poster): Poster {
        val directory = images.resolve("previews").resolve(poster.path.nameWithoutExtension)
        val previewDirectory = "${directory.parent.name}/${directory.name}"
        logger.info { "Generating previews for $previewDirectory" }

        if (directory.exists()) {
            logger.info { "Previews for $previewDirectory already exist, returning existing previews." }

            return poster.copy(previews=directory.listDirectoryEntries("*.png"))
        } else {
            directory.createDirectory()
        }

        val image = imageLoader.fromPath(poster.path)
        val upscaler = ImageUpscaler(upscaleWithRealESRGAN)
        val previews = fetchTemplatePaths(image)
            .map { path -> imageLoader.fromPath(path) }
            .map { template -> composePreview(template, image, directory) }
            .map { preview -> upscaler.upscale(preview, MAX_PIXELS_PER_SIDE_PREVIEW, true) }

        return poster.copy(previews=previews)
    }

    private fun fetchTemplatePaths(image: ImmutableImage): List<Path> {
        val orientation = when {
            image.width > image.height -> "horizontal"
            image.width < image.height -> "vertical"
            else -> "square"
        }

        return images.resolve("templates").resolve(orientation)
            .listDirectoryEntries("*.png")
            .shuffled()
            .take(NUMBER_OF_TEMPLATES)
    }

    private fun composePreview(
        template: ImmutableImage,
        poster: ImmutableImage,
        outputPath: Path
    ): Path {
        val (x, y, w, h) = locateFrameIn(template)
        val previewPath = outputPath.resolve("${UUID.randomUUID()}.png")
        val shadow = imageLoader.fromPath(images.resolve("shadow.png"))

        return template
            .overlay(poster.cover(w, h), x, y)
            .overlay(shadow.flipX().scaleTo(w, h), x, y)
            .output(PngWriter(), previewPath)
    }

    private fun locateFrameIn(image: ImmutableImage): GreenScreen {
        val isGreen = { pixel: Pixel -> pixel.toRGB().contentEquals(intArrayOf(0, 255, 0)) }

        val centerX = image.width / 2
        val minY = generateSequence(0) { it + 1 }.takeWhile { !isGreen(image.pixel(centerX, it)) }.last()
        val maxY = generateSequence(image.height - 1) { it - 1 }.takeWhile { !isGreen(image.pixel(centerX, it)) }.last() + 1

        val centerY = (minY + maxY) / 2
        val minX = generateSequence(0) { it + 1 }.takeWhile { !isGreen(image.pixel(it, centerY)) }.last()
        val maxX = generateSequence(image.width - 1) { it - 1 }.takeWhile { !isGreen(image.pixel(it, centerY)) }.last() + 1

        return GreenScreen(minX, minY , maxX - minX, maxY - minY)
    }

}