package pipeline.steps

import pipeline.PipelineStep
import preview.Poster
import theme.Theme
import theme.ThemePrompter

class ThemeAllocationStep: PipelineStep() {
    private val prompter = ThemePrompter()

    override fun process(poster: Poster): Poster = poster.copy(theme = prompter.ask(poster.prompt))
    override fun shouldSkip(poster: Poster): Boolean = poster.theme != Theme.DEFAULT
}