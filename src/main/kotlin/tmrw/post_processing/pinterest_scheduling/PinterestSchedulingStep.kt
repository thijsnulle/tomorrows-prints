package tmrw.post_processing.pinterest_scheduling

import tmrw.INTERVAL_BETWEEN_POST
import tmrw.MAXIMUM_SIZE_BULK_UPLOAD_PINS
import tmrw.model.Print
import tmrw.post_processing.PostProcessingAggregate
import tmrw.post_processing.PostProcessingStep
import tmrw.utils.Files
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

class PinterestSchedulingStep(val batch: String): PostProcessingStep() {
    override fun process(prints: List<Print>, aggregate: PostProcessingAggregate): PostProcessingAggregate {
        val outputFolder = Files.social.resolve(batch)
        outputFolder.createDirectories()

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
                outputFolder.resolve("$batch-$index.csv")
                    .toFile()
                    .bufferedWriter()
                    .use { it.write("$csvHeaders\n${chunk.joinToString("\n")}") }
            }

        return aggregate
    }

    override fun shouldSkip(aggregate: PostProcessingAggregate): Boolean = Files.social.resolve(batch).exists()
}