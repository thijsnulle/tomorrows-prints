package tmrw.post_processing.video_preview_generation.preview_generator

import tmrw.model.Print
import java.nio.file.Path
import kotlin.math.max

private const val FRAME_COUNT = 90
private const val MAX_ZOOM = 100.0
private const val REVERSE_SPEEDUP = 3
private const val FRAME_COUNT_WITH_REVERSE = FRAME_COUNT * (REVERSE_SPEEDUP + 1) / REVERSE_SPEEDUP

class ZoomVideoPreviewGenerator: VideoPreviewGenerator(frameRate = 30, prefix = "zoom") {
    override fun generate(prints: List<Print>, inputFolder: Path): List<Path> = prints.map { print ->
        val image = loader.fromFile(print.printFile)
        val width = image.width
        val height = image.height

        (0 until FRAME_COUNT).fold(image) { acc, frame ->
            val zoom = 1 - (1 - 1 / MAX_ZOOM) * frame / FRAME_COUNT
            val newWidth = max((width * zoom).toInt(), 1)
            val newHeight = max((height * zoom).toInt(), 1)

            val img = acc.resizeTo(newWidth, newHeight).also { it
                .cover(1000, 1500)
                .output(writer, inputFolder.resolve("$frame.jpeg"))
            }

            val output = inputFolder.resolve("${FRAME_COUNT_WITH_REVERSE - (frame / REVERSE_SPEEDUP) - 1}.jpeg")

            if (frame % REVERSE_SPEEDUP == 0) img.cover(1000, 1500).output(writer, output)

            img
        }

        save(inputFolder, outputFolder(print), frameRate)
    }
}