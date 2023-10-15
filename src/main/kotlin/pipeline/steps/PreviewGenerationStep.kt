package pipeline.steps

import pipeline.PipelineStep
import preview.GreenScreenPreviewComposer
import preview.Poster

class PreviewGenerationStep: PipelineStep() {
    private val composer = GreenScreenPreviewComposer()

    override fun process(posters: List<Poster>): List<Poster> = posters.map { composer.compose(it) }
}