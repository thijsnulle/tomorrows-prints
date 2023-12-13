package tmrw.pipeline.thumbnail_generation

import tmrw.model.Print
import tmrw.pipeline.PipelineStep
import tmrw.pipeline.preview_generation.FramedPreviewGenerator
import tmrw.utils.Files

class ThumbnailGenerationStep: PipelineStep(maximumThreads = 4) {
    private val generator = FramedPreviewGenerator(previewFolder = Files.thumbnails, createSquarePreviews = true)

    override fun process(print: Print): Print = print.copy(thumbnail = generator.generate(print).random().toString())
    override fun postProcess(prints: List<Print>) {}

    override fun shouldSkip(print: Print): Boolean = print.thumbnail.isNotEmpty()
}