
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import pipeline.steps.*
import preview.Poster
import preview.PosterJsonObject
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.bufferedWriter
import kotlin.io.path.*

fun main() {
    println("Input JSON for the pipeline (leave empty for test file):")

    val input = Paths.get(readln().ifEmpty { null } ?: "src/main/resources/default.json").toAbsolutePath()
    val posters = Gson()
        .fromJson<List<PosterJsonObject>>(
            input.toFile().bufferedReader().use { it.readText() },
            object : TypeToken<List<PosterJsonObject>>() {}.type
        ).map { it.toPoster() }

    require(posters.all { it.path.exists() }) {
        "\nThe following image files do not exist in the `posters` folder:\n -" +
        posters.filterNot { it.path.exists() }.joinToString("\n -") { it.path.toString() }
    }

    val processedPosters: List<Poster> = listOf(
        ThemeAllocationStep(),
        ThumbnailGenerationStep(),
        PreviewGenerationStep(),
        PrintFileCreationStep(),
        PrintfulStep(),
    ).fold(posters) { current, step -> step.start(current) }

    backup(processedPosters, Paths.get("src/main/resources/social/test.json").toAbsolutePath())
}

// TODO: move this method into a `PosterUtils` class
private fun backup(posters: List<Poster>, output: Path) {
    val content = Gson().toJson(posters.map {
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

    output.toFile().bufferedWriter().use { it.write(content) }
}
