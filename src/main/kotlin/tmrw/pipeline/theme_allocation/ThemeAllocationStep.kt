package tmrw.pipeline.theme_allocation

import tmrw.model.Print
import tmrw.pipeline.PipelineStep

class ThemeAllocationStep: PipelineStep() {
    private val prompter = ThemePrompter()

    override fun process(print: Print): Print = print.copy(theme = prompter.ask(print.prompt))
    override fun shouldSkip(print: Print): Boolean = print.theme != Theme.DEFAULT
}