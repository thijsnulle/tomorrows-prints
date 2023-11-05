
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import pipeline.steps.*
import preview.Poster
import preview.PosterJsonObject
import social.pinterest.PinContent
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

    createPinSchedule(processedPosters, Paths.get("src/main/resources/social/test.json").toAbsolutePath())
}

private fun createPinSchedule(posters: List<Poster>, output: Path) {
    val pinContents = posters.map { poster ->
        poster.previews.map { preview ->
            PinContent(poster.prompt, poster.listingUrl, poster.theme.value, preview.toString())
        }.shuffled()
    }

    val flattenedPinContents = pinContents.first().indices.flatMap { index ->
        pinContents.mapNotNull { it.getOrNull(index) }
    }

    val content = GsonBuilder().setPrettyPrinting().create().toJson(flattenedPinContents.map {
        val jsonObject = JsonObject()

        jsonObject.addProperty("prompt", it.prompt)
        jsonObject.addProperty("listing", it.listing)
        jsonObject.addProperty("theme", it.theme)
        jsonObject.addProperty("preview", it.preview)

        jsonObject
    })

    output.toFile().bufferedWriter().use { it.write(content) }
}
