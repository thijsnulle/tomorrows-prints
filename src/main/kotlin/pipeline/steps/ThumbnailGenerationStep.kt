package pipeline.steps

import pipeline.PipelineStep
import preview.Poster
import utility.transformation.ThumbnailGenerator
import java.nio.file.Paths

class ThumbnailGenerationStep: PipelineStep() {
    private val generator = ThumbnailGenerator()

    override fun process(poster: Poster): Poster = poster.copy(thumbnail = generator.generateThumbnail(poster))
    override fun shouldSkip(poster: Poster): Boolean = poster.thumbnail != Paths.get("")
}