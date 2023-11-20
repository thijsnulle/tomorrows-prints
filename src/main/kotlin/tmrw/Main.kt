package tmrw
import deprecated.PrintfulStep
import tmrw.model.BatchPrint
import tmrw.model.JsonPrint
import tmrw.model.Print
import tmrw.social.pinterest.PinContent
import tmrw.pipeline.preview_generation.PreviewGenerationStep
import tmrw.pipeline.preview_upload.PreviewUploadStep
import tmrw.pipeline.print_file_generation.PrintFileGenerationStep
import tmrw.pipeline.print_file_upload.PrintFileUploadStep
import tmrw.pipeline.size_guide_generation.SizeGuideGenerationStep
import tmrw.pipeline.theme_allocation.ThemeAllocationStep
import tmrw.pipeline.thumbnail_generation.ThumbnailGenerationStep
import tmrw.pipeline.title_allocation.TitleAllocationStep
import tmrw.utils.Files
import tmrw.utils.TeeOutputStream
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
//        TitleAllocationStep(),
        ThemeAllocationStep(),
//        ThumbnailGenerationStep(),
//        SizeGuideGenerationStep(),
        PreviewGenerationStep(),
        PreviewUploadStep(),
//        PrintFileGenerationStep(),
//        PrintFileUploadStep(),
//        PrintfulStep(),
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
    val defaultPinContents = prints.map {
        PinContent(it.prompt, it.title, it.listingUrl, "All Posters", it.path.toAbsolutePath().toString())
    }

    val pinContents = prints.map { print ->
        print.previews.map { preview ->
            PinContent(print.prompt, print.title, print.listingUrl, print.theme.value, preview.toAbsolutePath().toString())
        }.shuffled()
    }

    val allPinContents = defaultPinContents + pinContents.first().indices.flatMap { index ->
        pinContents.mapNotNull { it.getOrNull(index) }
    }

    val csvRows = prints.map { it.toCsvRow() }

    Files.storeAsJson(allPinContents, output)
}
