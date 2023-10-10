package pipeline

import preview.Poster

interface PipelineStep {
    fun process(posters: List<Poster>): List<Poster>
}