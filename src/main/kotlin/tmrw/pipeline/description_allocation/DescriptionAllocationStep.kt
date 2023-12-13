package tmrw.pipeline.description_allocation

import tmrw.model.Print
import tmrw.pipeline.PipelineStep

class DescriptionAllocationStep: PipelineStep() {
    private val prompter = DescriptionPrompter()

    override fun process(print: Print): Print = print.copy(description = prompter.ask(print.prompt))

    override fun postProcess(prints: List<Print>) {}

    override fun shouldSkip(print: Print): Boolean = print.description.isNotEmpty()
}