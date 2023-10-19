package pipeline.steps

import pipeline.PipelineStep
import preview.Poster
import utility.transformation.ThumbnailGenerator
import java.nio.file.Paths

class ThumbnailGenerationStep: PipelineStep() {
    private val generator = ThumbnailGenerator()

    override fun process(posters: List<Poster>): List<Poster> = posters.map {
        if (it.thumbnail == Paths.get("")) it.copy(thumbnail = generator.generateThumbnail(it)) else it
    }
}