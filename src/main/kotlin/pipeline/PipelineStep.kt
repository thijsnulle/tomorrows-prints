package pipeline

import io.github.oshai.kotlinlogging.KotlinLogging
import preview.Poster

abstract class PipelineStep {
    private val logger = KotlinLogging.logger {}

    fun start(posters: List<Poster>): List<Poster> {
        logger.info { "Starting ${this::class.simpleName}" }

        return process(posters)
    }

    abstract fun process(posters: List<Poster>): List<Poster>
}