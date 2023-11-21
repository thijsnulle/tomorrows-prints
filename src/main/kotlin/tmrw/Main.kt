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
//        TitleAllocationStep(),
//        DescriptionAllocationStep(),
//        ThemeAllocationStep(),
//        ThumbnailGenerationStep(),
//        SizeGuideGenerationStep(),
        PreviewGenerationStep(),
//        PreviewUploadStep(),
//        PrintFileGenerationStep(),
//        PrintFileUploadStep(),
    ).fold(prints) { current, step -> step.start(current) }

    createVideoPreviews(processedPrints)
    createPinSchedule(processedPrints, Files.social.resolve("$batch.csv"))
}

const val PRINTS_PER_VIDEO_PREVIEW = 18
const val FRAME_RATE_VIDEO_PREVIEW = 6

private fun createVideoPreviews(prints: List<Print>) {
    val videoPreviewsFolder = Files.previews.batchFolder(prints.first()).parent.resolve("videos").toAbsolutePath()
    videoPreviewsFolder.createDirectories()

    val temporaryDirectory = java.nio.file.Files.createTempDirectory("")
    val previewGenerator = FramedPreviewGenerator(previewFolder = temporaryDirectory, createSquarePreviews = true)

    val previewChunks = prints
        .map(previewGenerator::generate)
        .flatMap { it.previews }
        .shuffled()
        .chunked(PRINTS_PER_VIDEO_PREVIEW)

    previewChunks.forEach { previews ->
        if (previews.size != PRINTS_PER_VIDEO_PREVIEW) return@forEach

        temporaryDirectory.listDirectoryEntries("*.jpeg").forEach { it.deleteIfExists() }
        previews.forEach { java.nio.file.Files.copy(it, temporaryDirectory.resolve(it.fileName)) }

        val commandsToGenerateCompleteVideoPreview = listOf(
            "ffmpeg", "-y",
            "-framerate", FRAME_RATE_VIDEO_PREVIEW.toString(),
            "-pattern_type", "glob",
            "-i", "*.jpeg",
            "-c:v", "libx264",
            "-pix_fmt", "yuv420p",
            "$videoPreviewsFolder/${UUID.randomUUID()}.mp4"
        )

        ProcessBuilder(commandsToGenerateCompleteVideoPreview)
            .directory(temporaryDirectory.toFile())
            .start()
            .waitFor()
    }
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
    val initialDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).plusDays(1)
    val intervalInMinutes = INTERVAL_BETWEEN_POST * prints.size

    val csvHeaders = prints.first().toCsvHeaders()
    val csvRows = prints.mapIndexed { index, print ->
        val startDateTime = initialDateTime.plusMinutes(index * INTERVAL_BETWEEN_POST)

        print.toCsvRows(startDateTime, intervalInMinutes)
    }.flatten()

    csvRows
        .chunked(MAXIMUM_SIZE_BULK_UPLOAD_PINS)
        .forEachIndexed { index, chunk ->
            output.parent.resolve("${output.nameWithoutExtension}-$index.csv")
                .toFile()
                .bufferedWriter()
                .use { it.write("$csvHeaders\n${chunk.joinToString("\n")}") }
        }
}
