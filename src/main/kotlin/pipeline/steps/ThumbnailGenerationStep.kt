package pipeline.steps

import model.Print
import pipeline.PipelineStep
import utility.transformation.ThumbnailGenerator
import java.nio.file.Paths

class ThumbnailGenerationStep: PipelineStep() {
    private val generator = ThumbnailGenerator()

    override fun process(print: Print): Print = print.copy(thumbnail = generator.generateThumbnail(print))
    override fun shouldSkip(print: Print): Boolean = print.thumbnail != Paths.get("")
}