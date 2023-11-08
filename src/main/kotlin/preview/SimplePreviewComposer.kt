package preview

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import model.Print
import utility.files.Files
import java.awt.Color
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
    ).map { ImmutableImage.filled(SIZE, SIZE, it) }

    private val frames = Files.frames.toAbsolutePath()
        .listDirectoryEntries("*.png").map { loader.fromPath(it) }

    override fun compose(print: Print): Print {
        val printImage = loader.fromPath(print.path).cover(PRINT_WIDTH, PRINT_HEIGHT)
        val outputFolder = Files.previews.resolve(print.path.nameWithoutExtension).toAbsolutePath()

        val previews = backgrounds.flatMap { background ->
            frames.map { frame -> background
                .overlay(printImage, PRINT_X, PRINT_Y)
                .overlay(frame)
                .output(PngWriter(), outputFolder.resolve("${UUID.randomUUID()}.png"))
            }
        }

        return print.copy(previews = previews)
    }
}

