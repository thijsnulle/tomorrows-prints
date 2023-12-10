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
        ColourRotationVideoPreviewGenerator(),
        ZoomVideoPreviewGenerator(),
    )

    override fun process(prints: List<Print>, aggregate: PostProcessingAggregate): PostProcessingAggregate {
        Files.previews.batchFolder(prints.first()).parent.resolve("videos").toAbsolutePath()
            .createDirectories()

        return aggregate.copy(videoPreviews = videoPreviewGenerators.flatMap {
            try {
                it.generateVideoPreviews(prints)
            } catch (e: Exception) {
                emptyList()
            }
        })
    }
}