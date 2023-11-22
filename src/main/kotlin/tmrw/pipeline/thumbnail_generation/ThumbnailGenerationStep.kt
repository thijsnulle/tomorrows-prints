package tmrw.pipeline.thumbnail_generation

import tmrw.model.Print
import tmrw.pipeline.PipelineStep

class ThumbnailGenerationStep: PipelineStep() {
    private val generator = ThumbnailGenerator()

    override fun process(print: Print): Print {
        val thumbnailPath = generator.generateThumbnail(print)
        return print.copy(thumbnail = thumbnailPath.toString())
    }

    override fun shouldSkip(print: Print): Boolean = print.thumbnail.isNotEmpty()
}