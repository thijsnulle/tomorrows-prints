package tmrw.post_processing.video_preview_generation

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.JpegWriter
import tmrw.model.Print
import tmrw.pipeline.preview_generation.FramedPreviewGenerator
import tmrw.post_processing.PostProcessingAggregate
import tmrw.post_processing.PostProcessingStep
import tmrw.utils.Files
import tmrw.utils.Files.Companion.batchFolder
import java.awt.Color
import java.nio.file.Path
import java.util.*
import kotlin.io.path.*
import kotlin.random.Random

const val PRINTS_PER_VIDEO_PREVIEW = 30
const val CAROUSEL_SIZE = 12

class VideoPreviewGenerationStep: PostProcessingStep() {

    private val loader = ImmutableImage.loader()
    private val random = Random.Default
    private val writer = JpegWriter.compression(85).withProgressive(true)

    override fun process(prints: List<Print>, aggregate: PostProcessingAggregate): PostProcessingAggregate {
        val videoPreviewsFolder = Files.previews.batchFolder(prints.first()).parent.resolve("videos").toAbsolutePath()

        if (videoPreviewsFolder.exists()) {
            return aggregate.copy(
                videoPreviews = videoPreviewsFolder.listDirectoryEntries("*")
            )
        }

        videoPreviewsFolder.createDirectories()

        val carouselPreviews = prints
            .shuffled()
            .chunked(CAROUSEL_SIZE)
            .map { createCarouselPreview(it, videoPreviewsFolder) }
        val cyclePreviews = createCyclePreviews(prints, videoPreviewsFolder)
        val glitchPreviews = prints.map { createGlitchPreview(it, videoPreviewsFolder) }

        val allVideoPreviews = carouselPreviews + cyclePreviews + glitchPreviews

        return aggregate.copy(videoPreviews = allVideoPreviews.shuffled())
    }

    private fun createCyclePreviews(prints: List<Print>, videoPreviewsFolder: Path): List<Path> {
        val temporaryDirectory = java.nio.file.Files.createTempDirectory("")
        val previewGenerator = FramedPreviewGenerator(previewFolder = temporaryDirectory, createSquarePreviews = true)

        val previewChunks = prints
            .map(previewGenerator::generate)
            .flatMap { it.previews }
            .shuffled()
            .chunked(PRINTS_PER_VIDEO_PREVIEW)

        return previewChunks.map { previews ->
            if (previews.size != PRINTS_PER_VIDEO_PREVIEW) return@map null

            temporaryDirectory.listDirectoryEntries("*.jpeg").forEach { it.deleteIfExists() }
            previews.forEachIndexed { index, preview ->
                java.nio.file.Files.copy(preview, temporaryDirectory.resolve("$index.jpeg"))
            }

            createVideoPreview(temporaryDirectory, videoPreviewsFolder, frameRate = 5)
        }.filterNotNull()
    }

    private fun createGlitchPreview(print: Print, videoPreviewsFolder: Path): Path {
        val skippedFrames = 6
        val totalFrames = 96

        val image = loader.fromPath(print.path)
        val temporaryDirectory = java.nio.file.Files.createTempDirectory("")

        (1..skippedFrames).forEach {
            image.output(writer, temporaryDirectory.resolve("$it.jpeg"))
            image.output(writer, temporaryDirectory.resolve("${totalFrames - it}.jpeg"))
        }

        val frames = (skippedFrames..(totalFrames / 2))

        frames.fold(image) { aggregateImage, frame ->
            val scaleFactor = random.nextDouble(0.25, 0.75)
            val scaledWidth = (image.width * scaleFactor).toInt()
            val scaledHeight = (image.height * scaleFactor).toInt()

            val x = random.nextInt(-scaledWidth / 2, image.width - scaledWidth / 2)
            val y = random.nextInt(-scaledHeight / 2, image.height - scaledHeight / 2)

            aggregateImage
                .overlay(image.scale(scaleFactor), x, y)
                .also { it.output(writer, temporaryDirectory.resolve("$frame.jpeg")) }
                .also { it.output(writer, temporaryDirectory.resolve("${totalFrames - frame}.jpeg")) }
        }

        return createVideoPreview(temporaryDirectory, videoPreviewsFolder, frameRate = 12)
    }

    fun createCarouselPreview(prints: List<Print>, videoPreviewsFolder: Path): Path {
        val temporaryDirectory = java.nio.file.Files.createTempDirectory("")
        val framesPerImage = 60
        val images = prints.map { loader.fromPath(it.path) }
        val emptyFrame = images.first().map { Color.WHITE }

        images.forEachIndexed { index, image ->
            val nextImage = images[(index + 1) % images.size]
            val option = random.nextInt(4)

            (0..framesPerImage).forEach { frameIndex ->
                val offsetX1 = (-image.width / (framesPerImage - 1) * frameIndex).coerceIn(-image.width, 0)
                val offsetX2 = (-image.width / (framesPerImage - 1) * frameIndex + image.width).coerceIn(0, image.width)
                val offsetY1 = (-image.height / (framesPerImage - 1) * frameIndex).coerceIn(-image.height, 0)
                val offsetY2 = (-image.height / (framesPerImage - 1) * frameIndex + image.height).coerceIn(0, image.height)

                val x1 = when (option % 4) {
                    0 -> offsetX1
                    2 -> -offsetX1
                    else -> 0
                }

                val x2 = when (option % 4) {
                    0 -> offsetX2
                    2 -> -offsetX2
                    else -> 0
                }

                val y1 = when (option % 4) {
                    1 -> offsetY1
                    3 -> -offsetY1
                    else -> 0
                }

                val y2 = when (option % 4) {
                    1 -> offsetY2
                    3 -> -offsetY2
                    else -> 0
                }

                emptyFrame
                    .overlay(image, x1, y1)
                    .overlay(nextImage, x2, y2)
                    .also { it.output(writer, temporaryDirectory.resolve("${index * framesPerImage + frameIndex}.jpeg")) }
            }
        }

        return createVideoPreview(temporaryDirectory, videoPreviewsFolder, frameRate = 75)
    }

    private fun createVideoPreview(directory: Path, videoPreviewsFolder: Path, frameRate: Int): Path {
        val output = videoPreviewsFolder.resolve("${UUID.randomUUID()}.mp4")
        val commandsToGenerateCompleteVideoPreview = listOf(
            "ffmpeg", "-y",
            "-framerate", frameRate.toString(),
            "-i", "%d.jpeg",
            "-c:v", "libx264",
            "-pix_fmt", "yuv420p",
            output.toString(),
        )

        ProcessBuilder(commandsToGenerateCompleteVideoPreview)
            .directory(directory.toFile())
            .start()
            .waitFor()

        return output
    }
}