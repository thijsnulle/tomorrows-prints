package preview

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.canvas.painters.LinearGradient
import com.sksamuel.scrimage.nio.PngWriter
import kotlinx.coroutines.*
import model.Print
import utility.files.Files
import utility.files.Files.Companion.batchFolderWithoutExtension
import java.awt.Color
import java.nio.file.Path
import java.util.*
import kotlin.io.path.listDirectoryEntries

const val SIZE = 2048
const val PRINT_WIDTH = 768
const val PRINT_HEIGHT = 1152
const val PRINT_X = (SIZE - PRINT_WIDTH) / 2
const val PRINT_Y = (SIZE - PRINT_HEIGHT) / 2

// TODO: add support for horizontal posters
class SimplePreviewComposer : PreviewComposer() {

    private val loader = ImmutableImage.loader()
    private val frames = Files.frames.listDirectoryEntries("*.png").map(loader::fromPath)
    private val gradient = LinearGradient.horizontal(Color.WHITE, Color.decode("#f2f2f2"))
    private val background = ImmutableImage.create(SIZE, SIZE).fill(gradient)

    override fun compose(print: Print): Print = runBlocking {
        val printImage = loader.fromPath(print.path).cover(PRINT_WIDTH, PRINT_HEIGHT)
        val outputFolder = Files.previews.batchFolderWithoutExtension(print)

        val previews = frames.map { frame ->
            async(Dispatchers.Default) {
                processImage(background, frame, printImage, outputFolder)
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


