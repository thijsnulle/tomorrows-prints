package tmrw.post_processing

import io.github.oshai.kotlinlogging.KotlinLogging
import tmrw.model.Print
import java.nio.file.Path
import kotlin.time.Duration
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

data class PostProcessingAggregate(
    val videoPreviews: List<Path> = emptyList(),
    val videoPreviewUrls: List<String> = emptyList(),
)

abstract class PostProcessingStep {

    private val logger = KotlinLogging.logger {}

    fun start(prints: List<Print>, aggregate: PostProcessingAggregate): PostProcessingAggregate {
        logger.info { "Starting ${this::class.simpleName}" }

        val (updatedAggregate: PostProcessingAggregate, duration: Duration) = measureTimedValue {
            process(prints, aggregate)
        }

        logger.info { "Post-Processing ${this::class.simpleName} took ${duration.inWholeMilliseconds} ms" }

        return updatedAggregate
    }

    abstract fun process(prints: List<Print>, aggregate: PostProcessingAggregate): PostProcessingAggregate
}