package pipeline.steps

import model.Print
import pipeline.PipelineStep
import preview.SimplePreviewComposer

class PreviewGenerationStep: PipelineStep() {
    private val composer = SimplePreviewComposer()

    override fun process(print: Print): Print = composer.composePreviewsFor(print)
    override fun shouldSkip(print: Print): Boolean = print.previews.isNotEmpty()
}