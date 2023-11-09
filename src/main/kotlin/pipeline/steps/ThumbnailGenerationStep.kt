package pipeline.steps

import model.Print
import pipeline.PipelineStep
import utility.transformation.ThumbnailGenerator

class ThumbnailGenerationStep: PipelineStep() {
    private val generator = ThumbnailGenerator()

    override fun process(print: Print): Print {
        val thumbnailPath = generator.generateThumbnail(print)
        return print.copy(thumbnail = thumbnailPath.toString())
    }

    override fun shouldSkip(print: Print): Boolean = print.thumbnail.isNotEmpty()
}