
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
          [1] Load prints from backup file.
          [2] Load prints from batch file.
        
        Selected choice: 
    """.trimIndent())

    val choice = readln().ifEmpty { null } ?: "1"
    val fileChoice = if (choice == "1") "default" else "batch"

    println("\nInput file, leave empty to use $fileChoice.json: ")
    val input = Paths.get(readln().ifEmpty { null } ?: "src/main/resources/$fileChoice.json")

    val prints = if (choice == "1") Files.loadFromJson<JsonPrint>(input).map { it.toPrint() } else
            Files.loadFromJson<BatchPrint>(input).map { it.toPrint() }

    require(prints.all { it.path.exists() }) {
        "\nThe following image files do not exist in the `prints` folder:\n -" +
        prints.filterNot { it.path.exists() }.joinToString("\n -") { it.path.toString() }
    }

    val processedPrints: List<Print> = listOf(
        ThemeAllocationStep(),
        ThumbnailGenerationStep(),
        PreviewGenerationStep(),
        PrintFileCreationStep(),
        PrintFileUploadStep(),
        PrintfulStep(),
    ).fold(prints) { current, step -> step.start(current) }

    createPinSchedule(processedPrints, Files.social.resolve("schedule.json"))
}

private fun createPinSchedule(prints: List<Print>, output: Path) {
    val pinContents = prints.map { print ->
        print.previews.map { preview ->
            PinContent(print.prompt, print.listingUrl, print.theme.value, preview.toAbsolutePath().toString())
        }.shuffled()
    }

    val flattenedPinContents = pinContents.first().indices.flatMap { index ->
        pinContents.mapNotNull { it.getOrNull(index) }
    }

    Files.storeAsJson(flattenedPinContents, output)
}
