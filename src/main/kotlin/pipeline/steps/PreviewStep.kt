package pipeline.steps

import pipeline.PipelineStep
import preview.GreenScreenPreviewComposer
import preview.Poster

class PreviewStep: PipelineStep {
    private val composer = GreenScreenPreviewComposer()

    override fun process(posters: List<Poster>): List<Poster> = posters.map { composer.compose(it) }
}