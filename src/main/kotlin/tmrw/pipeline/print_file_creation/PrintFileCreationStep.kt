package tmrw.pipeline.print_file_creation

import tmrw.model.Print
import tmrw.pipeline.PipelineStep

class PrintFileCreationStep: PipelineStep(maximumThreads = 4) {
    private val printFileCreator = PrintFileCreator()

    override fun process(print: Print): Print = printFileCreator.create(print)
    override fun shouldSkip(print: Print): Boolean = print.printFileUrl.isNotEmpty()
}