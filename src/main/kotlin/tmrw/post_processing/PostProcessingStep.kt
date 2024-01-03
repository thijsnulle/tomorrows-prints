package tmrw.post_processing

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.github.oshai.kotlinlogging.KotlinLogging
import tmrw.model.Print
import tmrw.utils.JsonMappable
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.time.Duration
import kotlin.time.measureTimedValue

data class PostProcessingAggregate(
    val videoPreviews: List<Path> = emptyList(),
    val videoPreviewUrls: List<String> = emptyList(),
): JsonMappable {
    override fun toJson(): JsonObject {
        val jsonObject = JsonObject()

        jsonObject.add("videoPreviews", JsonArray().also { videoPreviews.forEach { preview -> it.add(preview.toString()) }})
        jsonObject.add("videoPreviewUrls", JsonArray().also { videoPreviewUrls.forEach { preview -> it.add(preview) }})

        return jsonObject
    }
}

data class JsonPostProcessingAggregate(
    val videoPreviews: List<String>,
    val videoPreviewUrls: List<String>,
) {
    fun toAggregate() = PostProcessingAggregate(videoPreviews.map { Path(it) }, videoPreviewUrls)
}

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