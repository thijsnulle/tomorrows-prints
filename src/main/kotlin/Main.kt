
import model.BatchPrint
import model.JsonPrint
import model.Print
import pipeline.steps.*
import social.pinterest.PinContent
import utility.files.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.*

fun main() {
    print("""
        Please select what you want to do:
          [1] Load posters from backup file.
          [2] Load posters from batch file.
        
        Selected choice: 
    """.trimIndent())

    val choice = readln().ifEmpty { null } ?: "1"

    println("Input file, empty for test file: ")
    val input = Paths.get(readln().ifEmpty { null } ?: "src/main/resources/default.json").toAbsolutePath()

    val prints = if (choice == "1") Files.loadFromJson<JsonPrint>(input).map { it.toPrint() } else
            Files.loadFromJson<BatchPrint>(input).map { it.toPrint() }

    require(prints.all { it.path.exists() }) {
        "\nThe following image files do not exist in the `posters` folder:\n -" +
        prints.filterNot { it.path.exists() }.joinToString("\n -") { it.path.toString() }
    }

    val processedPosters: List<Print> = listOf(
        ThemeAllocationStep(),
        ThumbnailGenerationStep(),
        PreviewGenerationStep(),
        PrintFileCreationStep(),
        PrintfulStep(),
    ).fold(prints) { current, step -> step.start(current) }

    createPinSchedule(processedPosters, Files.social.resolve("schedule.json").toAbsolutePath())
}

private fun createPinSchedule(prints: List<Print>, output: Path) {
    val pinContents = prints.map { print ->
        print.previews.map { preview ->
            PinContent(print.prompt, print.listingUrl, print.theme.value, preview.toString())
        }.shuffled()
    }

    val flattenedPinContents = pinContents.first().indices.flatMap { index ->
        pinContents.mapNotNull { it.getOrNull(index) }
    }

    Files.storeAsJson(flattenedPinContents, output)
}
