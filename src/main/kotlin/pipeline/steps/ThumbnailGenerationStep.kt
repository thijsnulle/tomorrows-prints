package pipeline.steps

import pipeline.PipelineStep
import preview.Poster
import utility.transformation.ThumbnailGenerator

class ThumbnailGenerationStep: PipelineStep {
    private val generator = ThumbnailGenerator()

    override fun process(posters: List<Poster>): List<Poster> = posters.map {
        it.copy(thumbnail = generator.generateThumbnail(it))
    }
}