package pipeline.steps

import model.Print
import pipeline.PipelineStep
import theme.Theme
import theme.ThemePrompter

class ThemeAllocationStep: PipelineStep() {
    private val prompter = ThemePrompter()

    override fun process(print: Print): Print = print.copy(theme = prompter.ask(print.prompt))
    override fun shouldSkip(print: Print): Boolean = print.theme != Theme.DEFAULT
}