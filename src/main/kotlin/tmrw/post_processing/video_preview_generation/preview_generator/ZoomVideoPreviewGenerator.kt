package tmrw.post_processing.video_preview_generation.preview_generator

import tmrw.model.Print
import java.nio.file.Path

private const val FRAME_COUNT = 90
private const val MAX_ZOOM = 5.0

class ZoomVideoPreviewGenerator: VideoPreviewGenerator(frameRate = 30, prefix = "zoom") {
    override fun generate(prints: List<Print>, inputFolder: Path): List<Path> = prints.map { print ->
        val image = loader.fromFile(print.printFile)
        val width = image.width
        val height = image.height

        (0 until FRAME_COUNT).fold(image) { acc, frame ->
            println(frame)

            val zoom = 1 - (1 - 1 / MAX_ZOOM) / FRAME_COUNT * frame
            val newWidth = (width * zoom).toInt()
            val newHeight = (height * zoom).toInt()

            acc.resizeTo(newWidth, newHeight)
                .also { it.output(writer, inputFolder.resolve("$frame.jpeg")) }
        }

        save(inputFolder, outputFolder(print), frameRate)
    }
}