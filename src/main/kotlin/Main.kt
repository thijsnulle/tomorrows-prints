
import model.BatchPrint
import model.JsonPrint
import model.Print
import pipeline.steps.*
import social.pinterest.PinContent
import utility.files.Files
import utility.logging.TeeOutputStream
import java.io.FileOutputStream
import java.io.PrintStream
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import kotlin.io.path.*

fun main() {
    print("""
        Please select what you want to do:
          [1] Load prints from batch file.
          [2] Load prints from backup file.
        
        Selected choice: 
    """.trimIndent())

    val choice = readln().ifEmpty { null } ?: "1"
    val fileChoice = if (choice == "1") "batch" else "backup"

    println("\nInput file, leave empty to use $fileChoice.json: ")
    val input = Paths.get(readln().ifEmpty { null } ?: "src/main/resources/$fileChoice.json")
    val batch = input.nameWithoutExtension

    val prints = if (choice == "1") Files.loadFromJson<BatchPrint>(input).map { it.toPrint(batch) } else
        Files.loadFromJson<JsonPrint>(input).map { it.toPrint() }

    require(prints.all { it.path.exists() }) {
        "\nThe following image files do not exist in the `prints` folder:\n -" +
        prints.filterNot { it.path.exists() }.joinToString("\n -") { it.path.toString() }
    }

    enableLoggingToFile()

    val processedPrints: List<Print> = listOf(
        ThemeAllocationStep(),
        ThumbnailGenerationStep(),
        SizeGuideGenerationStep(),
        PreviewGenerationStep(),
        PrintFileCreationStep(),
        PrintFileUploadStep(),
        PrintfulStep(),
    ).fold(prints) { current, step -> step.start(current) }

    createPinSchedule(processedPrints, Files.social.resolve("$batch.json"))
}

private fun enableLoggingToFile() {
    val logFile = Files.logs.resolve("${LocalDateTime.now()}.log").toFile()
    val logFilePrintStream = FileOutputStream(logFile)

    val teeOutputStream = TeeOutputStream(System.out, logFilePrintStream)
    val printStream = PrintStream(teeOutputStream)

    System.setOut(printStream)
    System.setErr(printStream)

    Runtime.getRuntime().addShutdownHook(Thread {
        logFilePrintStream.close()
    })
}

private fun createPinSchedule(prints: List<Print>, output: Path) {
    val defaultPinContents = prints.map { PinContent(
        it.prompt,
        it.listingUrl,
        "All Posters",
        it.path.toAbsolutePath().toString()
    )}

    val pinContents = prints.map { print ->
        print.previews.map { preview ->
            PinContent(print.prompt, print.listingUrl, print.theme.value, preview.toAbsolutePath().toString())
        }.shuffled()
    }

    val allPinContents = defaultPinContents + pinContents.first().indices.flatMap { index ->
        pinContents.mapNotNull { it.getOrNull(index) }
    }

    Files.storeAsJson(allPinContents, output)
}
