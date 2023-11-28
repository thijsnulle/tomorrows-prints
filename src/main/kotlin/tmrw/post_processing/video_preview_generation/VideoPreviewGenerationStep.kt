package tmrw.post_processing.video_preview_generation

import tmrw.model.Print
import tmrw.post_processing.PostProcessingAggregate
import tmrw.post_processing.PostProcessingStep
import tmrw.post_processing.video_preview_generation.preview_generator.*
import tmrw.utils.Files
import tmrw.utils.Files.Companion.batchFolder
import kotlin.io.path.*

class VideoPreviewGenerationStep: PostProcessingStep() {

    private val videoPreviewGenerators = listOf(
        CarouselVideoPreviewGenerator(),
        CycleVideoPreviewGenerator(),
        GlitchVideoPreviewGenerator(),
        HueRotateVideoPreviewGenerator(),
    )

    override fun process(prints: List<Print>, aggregate: PostProcessingAggregate): PostProcessingAggregate {
        val videoPreviewsFolder = Files.previews.batchFolder(prints.first()).parent.resolve("videos").toAbsolutePath()

        if (videoPreviewsFolder.exists()) {
            return aggregate.copy(videoPreviews = videoPreviewsFolder.listDirectoryEntries("*"))
        }

        videoPreviewsFolder.createDirectories()

        return aggregate.copy(videoPreviews = videoPreviewGenerators.flatMap {
            it.generateVideoPreviews(prints)
        })
    }
}