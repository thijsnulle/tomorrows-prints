package tmrw.post_processing.video_preview_generation.preview_generator

import tmrw.model.Print
import java.nio.file.Path

const val GLITCH_SIZE = 18

class GlitchVideoPreviewGenerator: VideoPreviewGenerator(frameRate = 12) {
    override fun generate(prints: List<Print>, inputFolder: Path): List<Path> {
        val outputFolder = outputFolder(prints)

        return prints.map { print ->
            val samePrints = List(GLITCH_SIZE) { print }
            val randomPrints = prints.shuffled().take(GLITCH_SIZE)

            generateGlitchPreview(print, samePrints, inputFolder, outputFolder)
            generateGlitchPreview(print, randomPrints, inputFolder, outputFolder)
        }
    }

    private fun generateGlitchPreview(
        print: Print,
        prints: List<Print>,
        inputFolder: Path,
        outputFolder: Path,
    ): Path {
        val image = loader.fromPath(print.path)
            .also { it.output(writer, inputFolder.resolve("0.jpeg")) }
            .also { it.output(writer, inputFolder.resolve("${GLITCH_SIZE * 2 + 1}.jpeg")) }

        (0 until GLITCH_SIZE).fold(image) { aggregate, frame ->
            val scale = random.nextDouble(0.25, 0.75)
            val scaledWidth = (image.width * scale).toInt()
            val scaledHeight = (image.height * scale).toInt()

            val x = random.nextInt(-scaledWidth / 2, image.width - scaledWidth / 2)
            val y = random.nextInt(-scaledHeight / 2, image.height - scaledHeight / 2)

            val overlayImage = loader.fromPath(prints[frame].path)

            aggregate
                .overlay(overlayImage.scale(scale), x, y)
                .also { it.output(writer, inputFolder.resolve("${frame + 1}.jpeg")) }
                .also { it.output(writer, inputFolder.resolve("${GLITCH_SIZE * 2 - frame}.jpeg")) }
        }

        return save(inputFolder, outputFolder, frameRate)
    }
}