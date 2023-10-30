
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pipeline.steps.*
import preview.PosterJsonObject
import social.pinterest.PinterestInfluencer
import java.nio.file.Paths
import kotlin.io.path.exists

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

    val output = listOf(
        ThemeAllocationStep(),
        ThumbnailGenerationStep(),
        PreviewGenerationStep(),
        PrintFileCreationStep(),
        PrintfulStep(),
    ).fold(posters) { current, step -> step.start(current) }

    PinterestInfluencer().post(output)
}
