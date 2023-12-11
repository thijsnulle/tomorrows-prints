package tmrw.post_processing.pinterest_scheduling

import tmrw.model.MAX_NUMBER_OF_HASHTAGS
import tmrw.model.Print
import tmrw.post_processing.PostProcessingAggregate
import tmrw.post_processing.PostProcessingStep
import tmrw.utils.Files
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.math.max
import kotlin.math.min

const val MAXIMUM_SIZE_BULK_UPLOAD_PINS = 200
const val INTERVAL_BETWEEN_POST: Long = 30

data class Pin(
    val title: String,
    val mediaUrl: String,
    val board: String,
    val description: String,
    val link: String,
    val keywords: String,
) {
    fun toScheduledPin(publishDate: LocalDateTime): String {
        return "\"$title\",$mediaUrl,\"$board\",,\"$description\",$link?uuid=${UUID.randomUUID()},$publishDate,\"$keywords\""
    }
}

private const val CSV_HEADER = "Title,Media URL,Pinterest board,Thumbnail,Description,Link,Publish date,Keywords"

class PinterestSchedulingStep(val batch: String): PostProcessingStep() {

    override fun process(prints: List<Print>, aggregate: PostProcessingAggregate): PostProcessingAggregate {
        val outputFolder = Files.social.resolve(batch)

        if (outputFolder.exists()) return aggregate

        outputFolder.createDirectories()

        val showcasePins = prints.map { print -> Pin(
            title = "${print.title} • Tomorrow's Prints",
            mediaUrl = print.url,
            board = "All Posters",
            description = print.description,
            link = print.listingUrl,
            keywords = getTaggedTopics(),
        )}.shuffled()

        val previewPins = prints.flatMap { print ->
            print.previewUrls.mapIndexed { index, previewUrl -> Pin(
                title = "${print.title} • Tomorrow's Prints [${index + 1}/${print.previewUrls.size}]",
                mediaUrl = previewUrl,
                board = print.theme.value,
                description = decorateDescription(print),
                link = print.listingUrl,
                keywords = getTaggedTopics(),
            )}
        }.shuffled()

        val videoPreviewPins = aggregate.videoPreviewUrls.mapIndexed { index, videoPreviewUrl ->
            val printUuid = getUUID(videoPreviewUrl) ?: return@mapIndexed null
            val print = prints.firstOrNull { it.path.nameWithoutExtension == printUuid } ?: return@mapIndexed null

            Pin(
                title = "${title(videoPreviewUrl)} ${print.title} • Tomorrow's Prints",
                mediaUrl = videoPreviewUrl,
                board = "Video Posters",
                description = "", // TODO: create description for video previews
                link = print.listingUrl,
                keywords = getTaggedTopics(),
            )
        }.filterNotNull().shuffled()

        val allPins = interlace(showcasePins, previewPins, videoPreviewPins)
        val initialPublishDate = LocalDateTime.now().plusHours(1)
            .withMinute(0).withSecond(0).withNano(0)

        allPins
            .chunked(MAXIMUM_SIZE_BULK_UPLOAD_PINS)
            .forEachIndexed { batchIndex, pins ->
                val scheduledPinContents = pins.mapIndexed { pinIndex, pin ->
                    val publishDate = initialPublishDate.plusMinutes(INTERVAL_BETWEEN_POST * pinIndex)
                    pin.toScheduledPin(publishDate)
                }.joinToString("\n")

                outputFolder
                    .resolve("$batch-$batchIndex.csv")
                    .toFile()
                    .bufferedWriter()
                    .use { it.write("$CSV_HEADER\n$scheduledPinContents") }
            }

        return aggregate
    }
}

private fun getTaggedTopics() = Print.taggedTopics.shuffled().take(10).joinToString(",")

private fun getUUID(str: String): String? {
    val printUuidRegex = Regex("([a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12})")
    val matches = printUuidRegex.findAll(str).toList()

    return matches.getOrNull(matches.size - 2)?.groupValues?.get(1)
}

private fun title(fileName: String) = when {
    fileName.contains("carousel") -> "Carousel Wonders:"
    fileName.contains("colour-rotation") -> "All Shades:"
    fileName.contains("cycle") -> "Image Symphony:"
    fileName.contains("hue-rotate") -> "The Full Spectrum:"
    fileName.contains("glitch") -> "Pulsating:"
    fileName.contains("zoom") -> "Zoom Magic:"
    else -> "Simply Stunning:"
}

private fun decorateDescription(print: Print): String {
    val hashtags = Print.hashtags.shuffled().take(MAX_NUMBER_OF_HASHTAGS).joinToString(" ") { "#$it" }
    val callToAction = Print.callToActions.shuffled().first()

    return "${print.description} ${callToAction.replace("[link]", print.listingUrl)} $hashtags"
}

private fun <T> interlace(vararg lists: List<T>): List<T> {
    if (lists.isEmpty()) return emptyList()
    if (lists.size == 1) return lists.first()

    return lists.sortedBy { -it.size }.let {
        it.drop(1).fold(it.first(), ::interlace2)
    }
}

private fun <T> interlace2(l1: List<T>, l2: List<T>): List<T> {
    val minSize = min(l1.size, l2.size).coerceAtLeast(2)

    val chunksL1 = l1.chunked(l1.size / minSize)
    val chunksL2 = l2.chunked(l2.size / minSize)

    return (0 until max(chunksL1.size, chunksL2.size)).flatMap { i ->
        chunksL1.getOrNull(i).orEmpty() + chunksL2.getOrNull(i).orEmpty()
    }
}
