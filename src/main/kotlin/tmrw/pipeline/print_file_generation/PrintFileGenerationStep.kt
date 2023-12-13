package tmrw.pipeline.print_file_generation

import tmrw.model.Print
import tmrw.pipeline.PipelineStep

class PrintFileGenerationStep: PipelineStep(maximumThreads = 4) {
    private val printFileGenerator = PrintFileGenerator()

    override fun process(print: Print): Print = printFileGenerator.generate(print)
    override fun postProcess(prints: List<Print>) {}

    override fun shouldSkip(print: Print): Boolean = print.printFileUrl.isNotEmpty()
}