package tmrw.post_processing.video_preview_generation.preview_generator

import tmrw.model.Print
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.createDirectories
import kotlin.io.path.nameWithoutExtension

private const val FRAME_COUNT = 90
private const val MAX_ZOOM = 100.0
private const val REVERSE_SPEEDUP = 3
private const val FRAME_COUNT_WITH_REVERSE = FRAME_COUNT * (REVERSE_SPEEDUP + 1) / REVERSE_SPEEDUP

class ZoomVideoPreviewGenerator: VideoPreviewGenerator(frameRate = 30, prefix = "zoom") {
    override fun generate(prints: List<Print>, inputFolder: Path, outputFolder: Path): List<Path> = prints.mapIndexed { index, print ->
        val image = loader.fromFile(print.printFile)
        val width = image.width
        val height = image.height

        val personalInputFolder = inputFolder.resolve(UUID.randomUUID().toString()).createDirectories()

        (0 until FRAME_COUNT).fold(image) { acc, frame ->
            System.gc()

            val zoom = 1 - (1 - 1 / MAX_ZOOM) * frame / FRAME_COUNT
            val newWidth = (width * zoom).toInt()
            val newHeight = (height * zoom).toInt()

            val img = acc.resizeTo(newWidth, newHeight).also { it
                .fit(1000, 1500)
                .let { resizedImage ->
                    val resizedImagePath = resizedImage.output(writer, personalInputFolder.resolve("$frame.jpeg"))

                    if (frame % REVERSE_SPEEDUP == 0) {
                        val reversedResizedImagePath = personalInputFolder.resolve("${FRAME_COUNT_WITH_REVERSE - (frame / REVERSE_SPEEDUP) - 1}.jpeg")

                        Files.copy(resizedImagePath, reversedResizedImagePath)
                    }
                }
            }

            img
        }

        progress(prints, index)

        save(personalInputFolder, output(outputFolder, print), frameRate)
    }
}
