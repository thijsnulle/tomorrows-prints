package pipeline.steps

import pipeline.PipelineStep
import preview.Poster
import theme.ThemePrompter

class ThemeAllocationStep: PipelineStep() {
    private val prompter = ThemePrompter()

    override fun process(posters: List<Poster>): List<Poster> = posters.map { it.copy(theme = prompter.ask(it.prompt)) }
}