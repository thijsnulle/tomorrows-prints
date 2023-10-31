package pipeline.steps

import pipeline.PipelineStep
import preview.Poster
import preview.SimplePreviewComposer

class PreviewGenerationStep: PipelineStep() {
    private val composer = SimplePreviewComposer()

    override fun process(poster: Poster): Poster = composer.compose(poster)
    override fun shouldSkip(poster: Poster): Boolean = poster.previews.isNotEmpty()
}