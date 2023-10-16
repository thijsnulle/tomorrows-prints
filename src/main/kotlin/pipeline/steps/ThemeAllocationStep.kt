package pipeline.steps

import pipeline.PipelineStep
import preview.Poster
import theme.ThemePromptHandler

class ThemeAllocationStep: PipelineStep() {
    private val prompter = ThemePromptHandler()

    override fun process(posters: List<Poster>): List<Poster> = posters.map { it.copy(theme = prompter.ask(it.prompt)) }
}