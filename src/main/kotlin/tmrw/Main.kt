package tmrw
import tmrw.model.BatchPrint
import tmrw.model.JsonPrint
import tmrw.model.Print
import tmrw.pipeline.colour_tagging.ColourAllocationStep
import tmrw.pipeline.description_allocation.DescriptionAllocationStep
import tmrw.pipeline.preview_generation.PreviewGenerationStep
import tmrw.pipeline.preview_upload.PreviewUploadStep
import tmrw.pipeline.print_file_generation.PrintFileGenerationStep
import tmrw.pipeline.print_file_upload.PrintFileUploadStep
import tmrw.pipeline.printful_synchronisation.PrintfulSynchronisationStep
import tmrw.pipeline.screenshot_generation.ScreenshotGenerationStep
import tmrw.pipeline.screenshot_upload.ScreenshotUploadStep
import tmrw.pipeline.shopify_publishing.ShopifyPublishingStep
import tmrw.pipeline.shopify_upload.ShopifyUploadStep
import tmrw.pipeline.size_guide_generation.SizeGuideGenerationStep
import tmrw.pipeline.size_guide_upload.SizeGuideUploadStep
import tmrw.pipeline.theme_allocation.ThemeAllocationStep
import tmrw.pipeline.thumbnail_generation.ThumbnailGenerationStep
import tmrw.pipeline.thumbnail_upload.ThumbnailUploadStep
import tmrw.pipeline.title_allocation.TitleAllocationStep
import tmrw.post_processing.PostProcessingAggregate
import tmrw.post_processing.pinterest_scheduling.PinterestSchedulingStep
import tmrw.post_processing.video_preview_generation.VideoPreviewGenerationStep
import tmrw.post_processing.video_preview_upload.VideoPreviewUploadStep
import tmrw.utils.Files
import java.nio.file.Paths
import kotlin.io.path.*

fun main() {
    print("""
        Please select what you want to do:
          [1] Load prints from batch file.
          [2] Load prints from backup file.
        
        Selected choice: 
    """.trimIndent())

    val choice = readln()
    val fileChoice = when (choice) {
        "1" -> "batch"
        "2" -> "backup"
        else -> throw IllegalArgumentException("Please provide a correct choice.")
    }

    println("\nInput file, leave empty to use $fileChoice.json: ")
    val input = Paths.get(readln().ifEmpty { null } ?: "src/main/resources/$fileChoice.json")
    val batch = input.nameWithoutExtension

    val prints = if (choice == "1") Files.loadFromJson<BatchPrint>(input).map { it.toPrint(batch) } else
        Files.loadFromJson<JsonPrint>(input).map { it.toPrint() }

    require(prints.all { it.path.exists() }) {
        "\nThe following image files do not exist in the `prints` folder:\n -" +
        prints.filterNot { it.path.exists() }.joinToString("\n -") { it.path.toString() }
    }

    Files.enableLoggingToFile()

    val processedPrints: List<Print> = listOf(
        TitleAllocationStep(),
        DescriptionAllocationStep(),
        ThemeAllocationStep(),
        ColourAllocationStep(),
        ThumbnailGenerationStep(),
        ThumbnailUploadStep(),
        SizeGuideGenerationStep(),
        SizeGuideUploadStep(),
        PreviewGenerationStep(),
        PreviewUploadStep(),
        PrintFileGenerationStep(),
        PrintFileUploadStep(),
        ScreenshotGenerationStep(),
        ScreenshotUploadStep(),
        ShopifyUploadStep(),
        PrintfulSynchronisationStep(),
        ShopifyPublishingStep(),
    ).fold(prints) { aggregate, step -> step.start(aggregate) }

    listOf(
        VideoPreviewGenerationStep(),
        VideoPreviewUploadStep(batch = batch),
        PinterestSchedulingStep(batch = batch),
    ).fold(PostProcessingAggregate()) { aggregate, step -> step.start(processedPrints, aggregate) }
}
