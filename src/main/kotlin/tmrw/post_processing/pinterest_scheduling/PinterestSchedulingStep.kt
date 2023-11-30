package tmrw.post_processing.pinterest_scheduling

import tmrw.model.Print
import tmrw.post_processing.PostProcessingAggregate
import tmrw.post_processing.PostProcessingStep
import tmrw.utils.Files
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

const val MAXIMUM_SIZE_BULK_UPLOAD_PINS = 200
const val INTERVAL_BETWEEN_POST: Long = 30

class PinterestSchedulingStep(val batch: String): PostProcessingStep() {
    override fun process(prints: List<Print>, aggregate: PostProcessingAggregate): PostProcessingAggregate {
        val outputFolder = Files.social.resolve(batch)

        if (outputFolder.exists()) return aggregate

        outputFolder.createDirectories()

        val initialDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).plusDays(1)
        val intervalInMinutes = INTERVAL_BETWEEN_POST * prints.size

        val csvHeader = "Title,Media URL,Pinterest board,Thumbnail,Description,Link,Publish date,Keywords"

        val showCaseCsvRows = prints.mapIndexed { index, print ->
            val publishDate = initialDateTime.plusMinutes(index * INTERVAL_BETWEEN_POST * print.previews.size)

            "\"${print.title} • Tomorrow's Prints\",${print.url},All Posters,,,${print.listingUrl},$publishDate,"
        }

        val previewCsvRows = prints.mapIndexed { index, print ->
            val startDateTime = initialDateTime.plusMinutes(index * INTERVAL_BETWEEN_POST)

            print.toCsvRows(startDateTime, intervalInMinutes)
        }.flatten()

        val totalTime = previewCsvRows.size * INTERVAL_BETWEEN_POST
        val videoPreviewCsvRows = aggregate.videoPreviewUrls.mapIndexed { index, videoPreviewUrl ->
            val publishDate = initialDateTime.plusMinutes(index * totalTime / aggregate.videoPreviewUrls.size)

            // TODO: add store URL
            "\"Exclusive Print Showcase • Tomorrow's Prints #${index + 1}\",$videoPreviewUrl,All Posters,,,#storeUrl,$publishDate,"
        }

        (showCaseCsvRows + previewCsvRows + videoPreviewCsvRows)
            .chunked(MAXIMUM_SIZE_BULK_UPLOAD_PINS)
            .forEachIndexed { index, chunk ->
                outputFolder.resolve("$batch-$index.csv")
                    .toFile()
                    .bufferedWriter()
                    .use { it.write("$csvHeader\n${chunk.joinToString("\n")}") }
            }

        return aggregate
    }
}