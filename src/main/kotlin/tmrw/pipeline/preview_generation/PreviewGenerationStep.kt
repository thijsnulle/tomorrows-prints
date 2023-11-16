package tmrw.pipeline.preview_generation

import tmrw.model.Print
import tmrw.pipeline.PipelineStep

class PreviewGenerationStep: PipelineStep() {
    private val composer = SimplePreviewComposer()

    override fun process(print: Print): Print = composer.composePreviewsFor(print)
    override fun shouldSkip(print: Print): Boolean = print.previews.isNotEmpty()
}