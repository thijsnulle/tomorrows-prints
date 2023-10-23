package pipeline.steps

import pipeline.PipelineStep
import preview.Poster
import prints.PrintFileCreator

class PrintFileCreationStep: PipelineStep() {
    private val printFileCreator = PrintFileCreator()

    override fun process(poster: Poster): Poster = printFileCreator.create(poster)
    override fun shouldSkip(poster: Poster): Boolean = poster.printFileUrl.isNotEmpty()
}