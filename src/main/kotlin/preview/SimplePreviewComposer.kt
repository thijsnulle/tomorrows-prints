package preview

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import com.sksamuel.scrimage.pixels.Pixel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Random
import kotlin.io.path.*
import kotlin.time.measureTime

data class GreenScreen(val x: Int, val y: Int, val w: Int, val h: Int)

class SimplePreviewComposer : PreviewComposer {

    private val imageLoader = ImmutableImage.loader()
    private val images = Paths.get("src/main/resources/images").toAbsolutePath()
    private val shadow = imageLoader.fromPath(images.resolve("shadow.png"))

    override fun compose(image: Image): Previews {
        val directory = images.resolve("previews").resolve(image.path.nameWithoutExtension)

        if (directory.exists()) {
            return Previews(directory.listDirectoryEntries("*.png"), image)
        } else {
            directory.createDirectory()
        }

        val poster = imageLoader.fromPath(image.path)
        val previews = images.resolve("templates")
            .listDirectoryEntries("*.png")
            .map { path -> imageLoader.fromPath(path) }
            .map { template -> composePreview(template, poster, directory) }

        return Previews(previews, image)
    }

    private fun composePreview(
        template: ImmutableImage,
        poster: ImmutableImage,
        outputPath: Path
    ): Path {
        val (x, y, w, h) = locateFrame(template)
        val previewPath = outputPath.resolve("preview-${System.currentTimeMillis()}.png")

        template
            .overlay(poster.cover(w, h), x, y)
            .overlay(shadow.flipX().scaleTo(w, h), x, y)
            .output(PngWriter(), previewPath)

        return previewPath
    }

    private fun locateFrame(image: ImmutableImage): GreenScreen {
        val centerX = image.width / 2
        val centerY = image.height / 2

        val isGreen = { pixel: Pixel -> pixel.toRGB().contentEquals(intArrayOf(0, 255, 0)) }
        val minX = generateSequence(centerX) { it - 1 }.takeWhile { isGreen(image.pixel(it, centerY)) }.last()
        val maxX = generateSequence(centerX) { it + 1 }.takeWhile { isGreen(image.pixel(it, centerY)) }.last()
        val minY = generateSequence(centerY) { it - 1 }.takeWhile { isGreen(image.pixel(centerX, it)) }.last()
        val maxY = generateSequence(centerY) { it + 1 }.takeWhile { isGreen(image.pixel(centerX, it)) }.last()

        return GreenScreen(minX, minY, maxX - minX + 1, maxY - minY + 1)
    }

}