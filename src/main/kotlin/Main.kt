import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pipeline.PipelineStep
import pipeline.steps.*
import preview.Poster
import preview.PosterJsonObject
import theme.Theme
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

fun main() {
    continueFromLatestBackup()
//    println("Input JSON for the pipeline: ")
//
//    val input = Paths.get(readln().ifEmpty { null } ?: "src/main/resources/default.json").toAbsolutePath()
//
//
//    require(posters.all { it.path.exists() }) {
//        "\nThe following image files do not exist in the `posters` folder:\n -" +
//        posters.filterNot { it.path.exists() }.joinToString("\n -") { it.path.toString() }
//    }
//

}

private fun runPipeline(posters: List<Poster>) {
    val pipeline: List<PipelineStep> = listOf(
        ThemeAllocationStep(),
        ThumbnailGenerationStep(),
        PreviewGenerationStep(),
        PrintFileCreationStep(),
//        SocialMediaStep(),
    )

    pipeline.fold(posters) { current, step -> step.start(current) }
}

private fun continueFromBackup(backupPath: Path) {
    val posters = Gson()
        .fromJson<List<PosterJsonObject>>(
            backupPath.toFile().bufferedReader().use { it.readText() },
            object : TypeToken<List<PosterJsonObject>>() {}.type
        ).map { it.toPoster() }

    runPipeline(posters)
}

private fun continueFromLatestBackup() {
    val latestBackup = Paths.get("src/main/resources/backups").listDirectoryEntries("*.json")
        .sortedBy { it.fileName }
        .last()

    continueFromBackup(latestBackup)
}
