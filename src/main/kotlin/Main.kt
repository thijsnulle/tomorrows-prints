
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import pipeline.steps.*
import preview.BatchPoster
import preview.Poster
import preview.PosterJsonObject
import social.pinterest.PinContent
import java.net.URI
import java.net.URL
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import kotlin.io.bufferedWriter
import kotlin.io.path.*

fun main() {
    print("""
        Please select what you want to do:
          [1] Load posters from backup file.
          [2] Load posters from batch file.
        
        Selected choice: 
    """.trimIndent())

    val choice = readln().ifEmpty { null } ?: "1"
    val posters = if (choice == "1") loadPostersFromBackup() else loadPostersFromBatch()

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

    createPinSchedule(processedPosters, Paths.get("src/main/resources/social/schedule.json").toAbsolutePath())
}

private fun loadPostersFromBackup(): List<Poster> {
    println("\nInput backup file (leave empty for test file):")

    val input = Paths.get(readln().ifEmpty { null } ?: "src/main/resources/default.json").toAbsolutePath()

    return Gson()
        .fromJson<List<PosterJsonObject>>(
            input.toFile().bufferedReader().use { it.readText() },
            object : TypeToken<List<PosterJsonObject>>() {}.type
        ).map { it.toPoster() }
}

private fun loadPostersFromBatch(): List<Poster> {
    println("\nInput batch file:")

    val input = Paths.get(readln()).toAbsolutePath()

    val batchPosters = Gson()
        .fromJson<List<BatchPoster>>(
            input.toFile().bufferedReader().use { it.readText() },
            object : TypeToken<List<BatchPoster>>() {}.type)

    val posters = batchPosters.map {
        val imageBytes = URI(it.url).toURL().readBytes()
        val image = ImmutableImage.loader().fromBytes(imageBytes)
        val path = image.output(PngWriter(), Paths.get("src/main/resources/images/posters/${UUID.randomUUID()}.png"))

        Poster(path, it.prompt)
    }

    return posters
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

    // TODO: put this into a separate `FileUtils` class (or inside the `PinContent` data class)
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
