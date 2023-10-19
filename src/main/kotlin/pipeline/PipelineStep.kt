package pipeline

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.github.oshai.kotlinlogging.KotlinLogging
import preview.Poster
import java.nio.file.Paths
import kotlin.io.path.name

abstract class PipelineStep {
    private val logger = KotlinLogging.logger {}
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val backupPath = Paths.get("src/main/resources/backups").toAbsolutePath()

    fun start(posters: List<Poster>): List<Poster> {
        logger.info { "Starting ${this::class.simpleName}" }
        val processedPosters = process(posters)

        logger.info { "Backing up pipeline results of ${this::class.simpleName}" }
        backup(processedPosters)

        return processedPosters
    }

    abstract fun process(posters: List<Poster>): List<Poster>
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

        val fileName = "${System.currentTimeMillis()}-${this::class.simpleName}.json"

        backupPath.resolve(fileName).toFile().bufferedWriter().use { it.write(content) }
    }
}