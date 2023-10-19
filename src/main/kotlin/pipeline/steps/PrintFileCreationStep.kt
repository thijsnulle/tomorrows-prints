package pipeline.steps

import pipeline.PipelineStep
import preview.Poster
import prints.PrintFileCreator

class PrintFileCreationStep: PipelineStep() {
    private val printFileCreator = PrintFileCreator()

    override fun process(posters: List<Poster>): List<Poster> = posters.map {
        if (it.printFileUrl.isEmpty()) printFileCreator.create(it) else it
    }
}