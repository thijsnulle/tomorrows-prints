package pipeline

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.github.oshai.kotlinlogging.KotlinLogging
import preview.Poster
import java.nio.file.Paths
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.name
import kotlin.time.Duration
import kotlin.time.measureTimedValue

abstract class PipelineStep {
    private val logger = KotlinLogging.logger {}
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val backupPath = Paths.get("src/main/resources/backups").toAbsolutePath()
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    fun start(posters: List<Poster>): List<Poster> {
        logger.info { "Starting ${this::class.simpleName}" }

        val (processedPosters: List<Poster>, duration: Duration) = measureTimedValue {
            posters.map { if (shouldSkip(it)) it else process(it) }
        }

        backup(processedPosters)

        logger.info { "Pipeline ${this::class.simpleName} took ${duration.inWholeMilliseconds} ms" }

        return processedPosters
    }

    abstract fun process(poster: Poster): Poster
    abstract fun shouldSkip(poster: Poster): Boolean

    private fun backup(posters: List<Poster>) {
        val content = gson.toJson(posters.map {
            val jsonObject = JsonObject()

            jsonObject.addProperty("path", it.path.name)
            jsonObject.addProperty("prompt", it.prompt)
            jsonObject.addProperty("theme", it.theme.value)

            val previews = JsonArray()
            it.previews.forEach { preview -> previews.add(preview.toString()) }
            jsonObject.add("previews", previews)

            jsonObject.addProperty("thumbnail", it.thumbnail.toString())
            jsonObject.addProperty("printFileUrl", it.printFileUrl)

            jsonObject
        })

        val fileName = "${dateFormatter.format(ZonedDateTime.now())}-${this::class.simpleName}.json"

        backupPath.resolve(fileName).toFile().bufferedWriter().use { it.write(content) }
    }
}