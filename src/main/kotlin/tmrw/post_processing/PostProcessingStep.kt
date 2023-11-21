package tmrw.post_processing

import io.github.oshai.kotlinlogging.KotlinLogging
import tmrw.model.Print
import kotlin.time.measureTime

abstract class PostProcessingStep {

    private val logger = KotlinLogging.logger {}

    fun start(prints: List<Print>): List<Print> {
        if (shouldSkip(prints)) {
            logger.info { "Skipping ${this::class.simpleName}" }
            return prints
        }

        logger.info { "Starting ${this::class.simpleName}" }

        val duration = measureTime {
            process(prints)
        }

        logger.info { "Post-Processing ${this::class.simpleName} took ${duration.inWholeMilliseconds} ms" }

        return prints
    }

    abstract fun process(prints: List<Print>)
    abstract fun shouldSkip(prints: List<Print>): Boolean

}