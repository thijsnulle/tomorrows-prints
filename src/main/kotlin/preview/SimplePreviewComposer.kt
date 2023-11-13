package preview

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.canvas.painters.LinearGradient
import com.sksamuel.scrimage.nio.PngWriter
import kotlinx.coroutines.*
import model.Print
import utility.files.Files
import java.awt.Color
import java.nio.file.Path
import java.util.*
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension

const val SIZE = 2048
const val PRINT_WIDTH = 768
const val PRINT_HEIGHT = 1152
const val PRINT_X = (SIZE - PRINT_WIDTH) / 2
const val PRINT_Y = (SIZE - PRINT_HEIGHT) / 2

// TODO: add support for horizontal posters
class SimplePreviewComposer : PreviewComposer() {

    private val loader = ImmutableImage.loader()

    private val backgrounds = listOf(
        Color(255, 255, 255),
        Color(255, 240, 240),
        Color(240, 240, 255),
    ).map { from ->
        val to = Color((from.red * 0.9).toInt(), (from.green * 0.9).toInt(), (from.blue * 0.9).toInt())
        ImmutableImage.create(SIZE, SIZE).fill(LinearGradient.horizontal(from, to))
    }

    private val frames = Files.frames.listDirectoryEntries("*.png").map(loader::fromPath)

    override fun compose(print: Print): Print = runBlocking {
        val printImage = loader.fromPath(print.path).cover(PRINT_WIDTH, PRINT_HEIGHT)
        val outputFolder = Files.previews.resolve(print.path.nameWithoutExtension)

        val previews = backgrounds.flatMap { background ->
            frames.map { frame ->
                async(Dispatchers.Default) {
                    processImage(background, frame, printImage, outputFolder)
                }
            }
        }.awaitAll()

        print.copy(previews = previews)
    }

    private fun processImage(background: ImmutableImage, frame: ImmutableImage, print: ImmutableImage, outputFolder: Path): Path {
        return background
            .overlay(print, PRINT_X, PRINT_Y)
            .overlay(frame)
            .output(PngWriter(), outputFolder.resolve("${UUID.randomUUID()}.png"))
    }
}


