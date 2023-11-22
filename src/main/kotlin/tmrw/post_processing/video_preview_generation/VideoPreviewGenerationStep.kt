package tmrw.post_processing.video_preview_generation

import tmrw.model.Print
import tmrw.pipeline.preview_generation.FramedPreviewGenerator
import tmrw.post_processing.PostProcessingAggregate
import tmrw.post_processing.PostProcessingStep
import tmrw.utils.Files
import tmrw.utils.Files.Companion.batchFolder
import java.util.*
import kotlin.io.path.*

const val PRINTS_PER_VIDEO_PREVIEW = 30
const val FRAME_RATE_VIDEO_PREVIEW = 5

class VideoPreviewGenerationStep: PostProcessingStep() {
    override fun process(prints: List<Print>, aggregate: PostProcessingAggregate): PostProcessingAggregate {
        val videoPreviewsFolder = Files.previews.batchFolder(prints.first()).parent.resolve("videos").toAbsolutePath()

        if (videoPreviewsFolder.exists()) {
            return aggregate.copy(
                videoPreviews = videoPreviewsFolder.listDirectoryEntries("*")
            )
        }

        videoPreviewsFolder.createDirectories()

        val temporaryDirectory = java.nio.file.Files.createTempDirectory("")
        val previewGenerator = FramedPreviewGenerator(previewFolder = temporaryDirectory, createSquarePreviews = true)

        val previewChunks = prints
            .map(previewGenerator::generate)
            .flatMap { it.previews }
            .shuffled()
            .chunked(PRINTS_PER_VIDEO_PREVIEW)

        val videoPreviews = previewChunks.map { previews ->
            if (previews.size != PRINTS_PER_VIDEO_PREVIEW) return@map null

            temporaryDirectory.listDirectoryEntries("*.jpeg").forEach { it.deleteIfExists() }
            previews.forEach { java.nio.file.Files.copy(it, temporaryDirectory.resolve(it.fileName)) }

            val output = "$videoPreviewsFolder/${UUID.randomUUID()}.mp4"
            val commandsToGenerateCompleteVideoPreview = listOf(
                "ffmpeg", "-y",
                "-framerate", FRAME_RATE_VIDEO_PREVIEW.toString(),
                "-pattern_type", "glob",
                "-i", "*.jpeg",
                "-c:v", "libx264",
                "-pix_fmt", "yuv420p",
                output,
            )

            ProcessBuilder(commandsToGenerateCompleteVideoPreview)
                .directory(temporaryDirectory.toFile())
                .start()
                .waitFor()

            Path(output)
        }.filterNotNull()

        return aggregate.copy(videoPreviews = videoPreviews)
    }
}