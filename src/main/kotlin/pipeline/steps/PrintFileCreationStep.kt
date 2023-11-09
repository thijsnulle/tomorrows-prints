package pipeline.steps

import model.Print
import pipeline.PipelineStep
import prints.PrintFileCreator

class PrintFileCreationStep: PipelineStep(maximumThreads = 6) {
    private val printFileCreator = PrintFileCreator()

    override fun process(print: Print): Print = printFileCreator.create(print)
    override fun shouldSkip(print: Print): Boolean = print.printFileUrl.isNotEmpty()
}