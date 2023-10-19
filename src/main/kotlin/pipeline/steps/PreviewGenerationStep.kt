package pipeline.steps

import pipeline.PipelineStep
import preview.GreenScreenPreviewComposer
import preview.Poster

class PreviewGenerationStep: PipelineStep() {
    private val composer = GreenScreenPreviewComposer()

    override fun process(posters: List<Poster>): List<Poster> = posters.map {
        if (it.previews.isEmpty()) composer.compose(it) else it
    }
}