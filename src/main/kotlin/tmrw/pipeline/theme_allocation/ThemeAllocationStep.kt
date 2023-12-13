package tmrw.pipeline.theme_allocation

import tmrw.model.Print
import tmrw.pipeline.PipelineStep

class ThemeAllocationStep: PipelineStep() {
    private val prompter = ThemePrompter()

    override fun process(print: Print): Print {
        val theme = (1..3).map { prompter.ask(print.prompt) }
            .groupingBy { it }.eachCount()
            .maxBy { it.value }.key

        return print.copy(theme = theme)
    }

    override fun postProcess(prints: List<Print>) {}

    override fun shouldSkip(print: Print): Boolean = print.theme != Theme.DEFAULT
}