import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pipeline.PipelineStep
import pipeline.steps.*
import preview.Poster
import java.nio.file.Paths
import kotlin.io.path.exists

data class PosterInfo(val path: String, val prompt: String)

fun main() {
    print("Input JSON for the pipeline: ")

    val input = Paths.get(readln().ifEmpty { null } ?: "src/main/resources/default.json").toAbsolutePath()
    val posters = Gson()
        .fromJson<List<PosterInfo>>(
            input.toFile().bufferedReader().use { it.readText() },
            object : TypeToken<List<PosterInfo>>() {}.type
        )
        .map { Poster(it.path, it.prompt) }

    require(posters.all { it.path.exists() }) {
        "\nThe following image files do not exist in the `posters` folder:\n -" +
        posters.filterNot { it.path.exists() }.joinToString("\n -") { it.path.toString() }
    }

    val pipeline: List<PipelineStep> = listOf(
        ThemeAllocationStep(),
        ThumbnailGenerationStep(),
        PreviewGenerationStep(),
        PrintFileCreationStep(),
        SocialMediaStep(),
    )

    pipeline.fold(posters) { current, step -> step.start(current) }
}
