package tmrw.post_processing.pinterest_scheduling

import io.github.cdimascio.dotenv.dotenv
import tmrw.model.MAX_NUMBER_OF_HASHTAGS
import tmrw.model.Print
import tmrw.post_processing.JsonPostProcessingAggregate
import tmrw.post_processing.PostProcessingAggregate
import tmrw.post_processing.PostProcessingStep
import tmrw.utils.Files
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.nameWithoutExtension
import kotlin.math.max
import kotlin.math.min

private const val NUMBER_OF_UPLOADS_PER_HOUR = 2
private const val NUMBER_OF_TAGGED_TOPICS = 50
private val INITIAL_DATE = LocalDate.of(2024, Month.JANUARY, 1)

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

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun process(prints: List<Print>, aggregate: PostProcessingAggregate): PostProcessingAggregate {
        val outputFolder = Files.social.resolve(batch)

        if (outputFolder.exists()) return aggregate

        outputFolder.createDirectories()

        val showcasePins = prints.map { print -> Pin(
            title = "Now Available! ${print.title} • Tomorrow's Prints",
            mediaUrl = print.url,
            board = "All Posters",
            description = decorateDescription(print),
            link = print.listingUrl.replace(dotenv()["SHOPIFY_STORE"], dotenv()["STORE_DOMAIN"]),
            keywords = keywords(),
        )}.shuffled()

        val previewPins = prints.map { print ->
            print.previewUrls.random().let { Pin(
                title = "${print.title} • Tomorrow's Prints",
                mediaUrl = it,
                board = print.theme.value,
                description = decorateDescription(print),
                link = print.listingUrl
                    .replace(dotenv()["SHOPIFY_STORE"], dotenv()["STORE_DOMAIN"]),
                keywords = keywords(),
            ) }
        }.shuffled()

        val videoPreviewPins = aggregate.videoPreviewUrls.mapIndexed { index, videoPreviewUrl ->
            val printUuid = getUUID(videoPreviewUrl) ?: return@mapIndexed null
            val print = prints.firstOrNull { it.path.nameWithoutExtension == printUuid } ?: return@mapIndexed null

            Pin(
                title = "$cta: ${print.title} • Tomorrow's Prints",
                mediaUrl = videoPreviewUrl,
                board = "Video Posters",
                description = decorateDescription(print),
                link = print.listingUrl.replace(dotenv()["SHOPIFY_STORE"], dotenv()["STORE_DOMAIN"]),
                keywords = keywords(),
            )
        }.filterNotNull().shuffled()

        val allPins = interlace(showcasePins, previewPins, videoPreviewPins)

        allPins
            .chunked(NUMBER_OF_UPLOADS_PER_HOUR * 24)
            .forEachIndexed { day, pins ->
                val date = INITIAL_DATE.plusDays(day.toLong()).atStartOfDay()

                val scheduledPinContents = pins.mapIndexed { pinIndex, pin ->
                    val interval = 60L / NUMBER_OF_UPLOADS_PER_HOUR
                    val publishDate = date.plusMinutes(interval * pinIndex)

                    pin.toScheduledPin(publishDate)
                }.joinToString("\n")

                outputFolder
                    .resolve("${formatter.format(date)}.csv")
                    .toFile()
                    .bufferedWriter()
                    .use { it.write("$CSV_HEADER\n$scheduledPinContents") }
            }

        return aggregate
    }
}

private fun keywords() = Print.taggedTopics.shuffled().take(NUMBER_OF_TAGGED_TOPICS).joinToString(",")

private fun getUUID(str: String): String? {
    val printUuidRegex = Regex("([a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12})")
    val matches = printUuidRegex.findAll(str).toList()

    return matches.getOrNull(matches.size - 2)?.groupValues?.get(1)
}

private val cta get() = listOf(
    "Buy Now",
    "Shop Today",
    "Order Today",
    "Don't Miss Out",
    "Get Yours Today",
    "Shop Art Prints",
    "Secure Your Print",
    "Revamp Your Space",
    "Elevate Your Space",
    "Immerse Your Space",
    "Make Your Walls Pop",
    "Decorate Your Walls",
    "Transform Your Walls",
    "Buy Your Masterpiece",
    "Shop Now, Frame Later",
    "Order Your Poster Now",
    "Elevate Your Artistry",
    "Grab Your Print Today",
    "Find Your Masterpiece",
    "Explore the Collection",
    "Personalise Your Space",
    "Redefine Your Interior",
    "Elevate Your Aesthetic",
    "Discover Our Collection",
    "Unleash Your Creativity",
).shuffled().first()

private fun decorateDescription(print: Print): String {
    val hashtags = Print.hashtags.shuffled().take(MAX_NUMBER_OF_HASHTAGS).joinToString(" ") { "#$it" }
    val callToAction = Print.callToActions.shuffled().first()

    return "${print.description} ${callToAction.replace("[link]", print.listingUrl.replace(dotenv()["SHOPIFY_STORE"], dotenv()["STORE_DOMAIN"]))} $hashtags"
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

    val chunksL1 = l1.chunked(max(l1.size / minSize, 1))
    val chunksL2 = l2.chunked(max(l2.size / minSize, 1))

    return (0 until max(chunksL1.size, chunksL2.size)).flatMap { i ->
        chunksL1.getOrNull(i).orEmpty() + chunksL2.getOrNull(i).orEmpty()
    }
}
