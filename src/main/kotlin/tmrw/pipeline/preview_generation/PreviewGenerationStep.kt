package tmrw.pipeline.preview_generation

import tmrw.model.Print
import tmrw.pipeline.PipelineStep

class PreviewGenerationStep: PipelineStep() {
    private val generator = FramedPreviewGenerator()

    override fun process(print: Print): Print = generator.generatePreviewsFor(print)
    override fun shouldSkip(print: Print): Boolean = print.previews.isNotEmpty()
}