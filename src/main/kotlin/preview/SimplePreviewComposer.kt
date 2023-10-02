package preview

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import com.sksamuel.scrimage.pixels.Pixel
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.*

data class GreenScreen(val x: Int, val y: Int, val w: Int, val h: Int)

const val NUMBER_OF_TEMPLATES = 15

class SimplePreviewComposer : PreviewComposer {

    private val imageLoader = ImmutableImage.loader()
    private val images = Paths.get("src/main/resources/images").toAbsolutePath()

    override suspend fun compose(poster: Poster): Poster {
        val directory = images.resolve("previews").resolve(poster.path.nameWithoutExtension)

        if (directory.exists()) {
            println("$directory already exists.")
            return poster.copy(previews=directory.listDirectoryEntries("*.png"))
        } else {
            directory.createDirectory()
        }

        val image = imageLoader.fromPath(poster.path)
        val previews = fetchTemplates(image)
            .map { path -> imageLoader.fromPath(path) }
            .map { template -> composePreview(template, image, directory) }

        return poster.copy(previews=previews)
    }

    private fun fetchTemplates(image: ImmutableImage): List<Path> {
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
        val previewPath = outputPath.resolve("preview-${System.currentTimeMillis()}.png")
        val shadow = imageLoader.fromPath(images.resolve("shadow.png"))

        template
            .overlay(poster.cover(w, h), x, y)
            .overlay(shadow.flipX().scaleTo(w, h), x, y)
            .output(PngWriter(), previewPath)

        return previewPath
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