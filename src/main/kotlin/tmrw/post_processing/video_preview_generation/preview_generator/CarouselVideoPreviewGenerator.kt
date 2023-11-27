package tmrw.post_processing.video_preview_generation.preview_generator

import tmrw.model.Print
import java.awt.Color
import java.nio.file.Path

const val VIDEO_PREVIEW_CAROUSEL_SIZE = 5

class CarouselVideoPreviewGenerator: VideoPreviewGenerator(frameRate = 60) {

    override fun generate(prints: List<Print>, inputFolder: Path): List<Path> {
        val outputFolder = outputFolder(prints)

        return prints.map { print ->
            val remainingPrints = prints.shuffled().take(VIDEO_PREVIEW_CAROUSEL_SIZE - 1)
            val images = (listOf(print) + remainingPrints).map { loader.fromPath(it.path) }
            val frame = images.first().map { Color.WHITE }

            images.forEachIndexed { index, image ->
                val nextImage = images[(index + 1) % images.size]
                val direction = random.nextInt(4)

                (0..frameRate).forEach { frameIndex ->
                    val offsetX1 = (-image.width / (frameRate - 1) * frameIndex).coerceIn(-image.width, 0)
                    val offsetX2 = (-image.width / (frameRate - 1) * frameIndex + image.width).coerceIn(0, image.width)
                    val offsetY1 = (-image.height / (frameRate - 1) * frameIndex).coerceIn(-image.height, 0)
                    val offsetY2 = (-image.height / (frameRate - 1) * frameIndex + image.height).coerceIn(0, image.height)

                    fun calculateOffset(direction: Int, offset1: Int, offset2: Int): Int = when (direction % 4) {
                        0 -> offset1
                        2 -> -offset1
                        1 -> offset2
                        3 -> -offset2
                        else -> 0
                    }

                    val x1 = calculateOffset(direction, offsetX1, 0)
                    val x2 = calculateOffset(direction, offsetX2, 0)
                    val y1 = calculateOffset(direction, 0, offsetY1)
                    val y2 = calculateOffset(direction, 0, offsetY2)

                    frame
                        .overlay(image, x1, y1)
                        .overlay(nextImage, x2, y2)
                        .also { it.output(writer, inputFolder.resolve("${index * frameRate + frameIndex}.jpeg")) }
                }
            }

            save(inputFolder, outputFolder, frameRate)
        }
    }

}
