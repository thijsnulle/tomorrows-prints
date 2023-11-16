package tmrw.pipeline.title_allocation

import tmrw.model.Print
import tmrw.pipeline.PipelineStep

class TitleAllocationStep: PipelineStep() {
    private val prompter = TitlePromptHandler()

    override fun process(print: Print): Print = print.copy(title = prompter.ask(print.prompt))

    override fun shouldSkip(print: Print): Boolean = print.title.isNotEmpty()
}