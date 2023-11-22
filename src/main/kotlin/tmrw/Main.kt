package tmrw
import tmrw.model.BatchPrint
import tmrw.model.JsonPrint
import tmrw.model.Print
import tmrw.pipeline.description_allocation.DescriptionAllocationStep
import tmrw.pipeline.preview_generation.FramedPreviewGenerator
import tmrw.pipeline.preview_generation.PreviewGenerationStep
import tmrw.pipeline.preview_upload.PreviewUploadStep
import tmrw.pipeline.print_file_generation.PrintFileGenerationStep
import tmrw.pipeline.print_file_upload.PrintFileUploadStep
import tmrw.pipeline.size_guide_generation.SizeGuideGenerationStep
import tmrw.pipeline.theme_allocation.ThemeAllocationStep
import tmrw.pipeline.thumbnail_generation.ThumbnailGenerationStep
import tmrw.pipeline.title_allocation.TitleAllocationStep
import tmrw.post_processing.PostProcessingAggregate
import tmrw.post_processing.PostProcessingStep
import tmrw.post_processing.pinterest_scheduling.PinterestSchedulingStep
import tmrw.post_processing.video_preview_generation.VideoPreviewGenerationStep
import tmrw.post_processing.video_preview_upload.VideoPreviewUploadStep
import tmrw.utils.Files
import tmrw.utils.Files.Companion.batchFolder
import tmrw.utils.TeeOutputStream
import java.io.FileOutputStream
import java.io.PrintStream
import java.nio.file.Path
import java.nio.file.Paths
import java.time.*
import java.util.UUID
import kotlin.io.path.*

const val MAXIMUM_SIZE_BULK_UPLOAD_PINS = 200
const val INTERVAL_BETWEEN_POST: Long = 30

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
        TitleAllocationStep(),
        DescriptionAllocationStep(),
        ThemeAllocationStep(),
        ThumbnailGenerationStep(),
        SizeGuideGenerationStep(),
        PreviewGenerationStep(),
        PreviewUploadStep(),
        PrintFileGenerationStep(),
        PrintFileUploadStep(),
    ).fold(prints) { aggregate, step -> step.start(aggregate) }

    listOf(
        VideoPreviewGenerationStep(),
        VideoPreviewUploadStep(batch = batch),
        PinterestSchedulingStep(batch = batch),
    ).fold(PostProcessingAggregate()) { aggregate, step -> step.start(processedPrints, aggregate) }
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
