package tmrw.pipeline.preview_generation

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.canvas.painters.LinearGradient
import com.sksamuel.scrimage.nio.JpegWriter
import kotlinx.coroutines.*
import tmrw.model.Print
import tmrw.utils.Files
import tmrw.utils.Files.Companion.batchFolderWithoutExtension
import java.awt.Color
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

const val SIZE = 2048
const val PRINT_WIDTH = 768
const val PRINT_HEIGHT = 1152

// TODO: add support for horizontal posters
class FramedPreviewGenerator(
    private val previewFolder: Path = Files.previews,
    private val createSquarePreviews: Boolean = false
): PreviewGenerator() {

    private val loader = ImmutableImage.loader()
    private val writer = JpegWriter.compression(85).withProgressive(true)
    private val random = Random()

    private val frames = Files.frames.listDirectoryEntries("*.png").map(loader::fromPath)
    private val gradient = LinearGradient.horizontal(Color.WHITE, Color.decode("#f2f2f2"))

    override fun generate(print: Print): List<Path> = runBlocking {
        val printImage = loader.fromPath(print.path).cover(PRINT_WIDTH, PRINT_HEIGHT)
        val outputFolder = previewFolder.batchFolderWithoutExtension(print)

        if (!outputFolder.exists()) outputFolder.createDirectories()

        return@runBlocking frames.map { frame ->
            async(Dispatchers.Default) {
                val background = ImmutableImage.create(
                    SIZE,
                    if (createSquarePreviews) SIZE else SIZE + random.nextInt(SIZE / 2)
                ).fill(gradient)

                processImage(background, frame, printImage, outputFolder)
            }
        }.awaitAll()
    }

    private fun processImage(background: ImmutableImage, frame: ImmutableImage, print: ImmutableImage, outputFolder: Path): Path {
        val x = (background.width - PRINT_WIDTH) / 2
        val y = (background.height - PRINT_HEIGHT) / 2

        return background
            .overlay(print, x, y)
            .overlay(frame, 0, (background.height - frame.height) / 2)
            .output(writer, outputFolder.resolve("${UUID.randomUUID()}.jpeg"))
    }
}


