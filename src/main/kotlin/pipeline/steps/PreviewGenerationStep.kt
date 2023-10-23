package pipeline.steps

import pipeline.PipelineStep
import preview.GreenScreenPreviewComposer
import preview.Poster

class PreviewGenerationStep: PipelineStep() {
    private val composer = GreenScreenPreviewComposer()

    override fun process(poster: Poster): Poster = composer.compose(poster)
    override fun shouldSkip(poster: Poster): Boolean = poster.previews.isNotEmpty()
}