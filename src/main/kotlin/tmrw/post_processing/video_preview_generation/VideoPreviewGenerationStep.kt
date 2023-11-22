package tmrw.post_processing.video_preview_generation

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.JpegWriter
import tmrw.model.Print
import tmrw.pipeline.preview_generation.FramedPreviewGenerator
import tmrw.post_processing.PostProcessingAggregate
import tmrw.post_processing.PostProcessingStep
import tmrw.utils.Files
import tmrw.utils.Files.Companion.batchFolder
import java.nio.file.Path
import java.util.*
import kotlin.io.path.*
import kotlin.random.Random

const val PRINTS_PER_VIDEO_PREVIEW = 30
const val FRAME_RATE_VIDEO_PREVIEW = 5

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

        val cyclePreviews = createCyclePreviews(prints, videoPreviewsFolder)
        val glitchPreviews = prints.map { createGlitchPreview(it, videoPreviewsFolder) }

        return aggregate.copy(videoPreviews = cyclePreviews + glitchPreviews)
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
            previews.forEach { java.nio.file.Files.copy(it, temporaryDirectory.resolve(it.fileName)) }

            val output = videoPreviewsFolder.resolve("${UUID.randomUUID()}.mp4")
            val commandsToGenerateCompleteVideoPreview = listOf(
                "ffmpeg", "-y",
                "-framerate", FRAME_RATE_VIDEO_PREVIEW.toString(),
                "-pattern_type", "glob",
                "-i", "*.jpeg",
                "-c:v", "libx264",
                "-pix_fmt", "yuv420p",
                output.toString(),
            )

            ProcessBuilder(commandsToGenerateCompleteVideoPreview)
                .directory(temporaryDirectory.toFile())
                .start()
                .waitFor()

            output
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

        val output = videoPreviewsFolder.resolve("${UUID.randomUUID()}.mp4")
        val commandsToGenerateCompleteVideoPreview = listOf(
            "ffmpeg", "-y",
            "-framerate", "12",
            "-i", "%d.jpeg",
            "-c:v", "libx264",
            "-pix_fmt", "yuv420p",
            output.toString(),
        )

        ProcessBuilder(commandsToGenerateCompleteVideoPreview)
            .directory(temporaryDirectory.toFile())
            .start()
            .waitFor()

        return output
    }
}