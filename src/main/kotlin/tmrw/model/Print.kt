package tmrw.model

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import io.github.oshai.kotlinlogging.KotlinLogging
import tmrw.pipeline.theme_allocation.Theme
import tmrw.utils.CsvMappable
import tmrw.utils.Files
import tmrw.utils.JsonMappable
import java.net.URI
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.time.Duration
import kotlin.time.measureTimedValue

const val MAX_NUMBER_OF_HASHTAGS = 3

data class Print(
    val path: Path,
    val prompt: String,
    val url: String = "",
    val theme: Theme = Theme.DEFAULT,
    val title: String = "",
    val description: String = "",
    val previews: List<Path> = emptyList(),
    val previewUrls: List<String> = emptyList(),
    val thumbnail: String = "",
    val sizeGuide: String = "",
    val printFile: String = "",
    val printFileUrl: String = "",
    val listingUrl: String = "",
) : JsonMappable, CsvMappable {
    constructor(fileName: String, prompt: String): this(Files.prints.resolve(fileName), prompt)

    companion object {
        private val hashtags = listOf(
            "aesthetic",
            "art",
            "artinspiration",
            "artist",
            "artwork",
            "decor",
            "decoratingideas",
            "design",
            "designideas",
            "gift",
            "gifts",
            "giftsforher",
            "giftsforhim",
            "giftideas",
            "homeinspiration",
            "interiordesign",
            "interiordesire",
            "interiorlovers",
            "interiors",
            "interiorstylist",
            "lifestyle",
            "modernhome",
            "pinterestart",
            "pinterestideas",
            "pinterestinspired",
            "walldecor",
        )

        private val callToActions = listOf(
            "See more: [link]",
            "Shop here: [link]",
            "Find yours: [link]",
            "Explore now: [link]",
            "Get it today: [link]",
            "Click to buy: [link]",
            "Get yours now: [link]",
            "Grab yours here: [link]",
            "Upgrade your walls: [link]",
            "Click and discover: [link]",
            "Shop the collection: [link]",
            "Click here to get yours now: [link]",
            "Redefine your home aesthetic: [link]",
            "Explore our exclusive posters: [link]",
            "Explore our poster collection: [link]",
            "Shop the poster collection now: [link]",
            "Dive into the collection  here: [link]",
            "Shop now for the perfect poster: [link]",
            "Unleash creativity on your walls: [link]",
            "Transform your space with a click: [link]",
            "Click to bring art into your space: [link]",
            "Don't miss out – explore our posters: [link]",
            "Your walls deserve an upgrade! Click here: [link]",
            "Discover our latest poster collection here: [link]",
            "Click the link to explore our poster gallery: [link]",
        )

        private val taggedTopics = listOf(
            "architecture poster",
            "art deco interior",
            "bedroom",
            "bedroom interior",
            "diy gifts",
            "diy wall art",
            "fashion poster",
            "graphic poster",
            "graphic design poster",
            "home interior design",
            "interior design tips",
            "living room",
            "luxury interior design",
            "minimalist poster",
            "modern interior",
            "music poster",
            "poster print",
            "print design",
            "retro poster",
            "wall art",
        )
    }

    // TODO: add test for this method
    override fun toJson(): JsonObject {
        val jsonObject = JsonObject()

        jsonObject.addProperty("path", "${path.parent.name}/${path.name}")
        jsonObject.addProperty("prompt", prompt)
        jsonObject.addProperty("url", url)
        jsonObject.addProperty("theme", theme.value)
        jsonObject.addProperty("title", title)
        jsonObject.addProperty("description", description)

        val previews = JsonArray()
        this.previews.forEach {
            preview -> previews.add(preview.toString())
        }
        jsonObject.add("previews", previews)

        val previewUrls = JsonArray()
        this.previewUrls.forEach {
                previewUrl -> previewUrls.add(previewUrl)
        }
        jsonObject.add("previewUrls", previewUrls)

        jsonObject.addProperty("thumbnail", thumbnail)
        jsonObject.addProperty("sizeGuide", sizeGuide)
        jsonObject.addProperty("printFile", printFile)
        jsonObject.addProperty("printFileUrl", printFileUrl)

        return jsonObject
    }

    override fun toCsvHeaders(): String = "Title,Media URL,Pinterest board,Thumbnail,Description,Link,Publish date,Keywords"

    override fun toCsvRows(startDate: LocalDateTime, intervalInMinutes: Long): List<String> {
        val getTaggedTopics = { taggedTopics.shuffled().take(10).joinToString(",") }

        // TODO: extract this from this method and interlace them in the final result (not at 30-minute interval)
        val generalCsvRow = "\"$title\",$url,${theme.value},,\"$description\",$listingUrl,$startDate,\"${getTaggedTopics()}\""

        val previewCsvRows = previewUrls.mapIndexed { index, previewUrl ->
            val publishDate = startDate.plusMinutes((index + 1) * intervalInMinutes)

            "\"$title [${index + 1}/${previewUrls.size}]\",${previewUrl},${theme.value},,\"${decorateDescription()}\",$listingUrl,$publishDate,\"${getTaggedTopics()}\""
        }

        return listOf(generalCsvRow) + previewCsvRows
    }

    private fun decorateDescription(): String {
        val hashtags = hashtags.shuffled().take(MAX_NUMBER_OF_HASHTAGS).joinToString(" "){ "#$it" }
        val callToAction = callToActions.shuffled().first()

        return "$description ${callToAction.replace("[link]", listingUrl)} $hashtags"
    }
}

data class JsonPrint(
    val path: String,
    val prompt: String,
    val url: String?,
    val theme: String?,
    val title: String?,
    val description: String?,
    val previews: List<String>?,
    val previewUrls: List<String>?,
    val thumbnail: String?,
    val sizeGuide: String?,
    val printFile: String?,
    val printFileUrl: String?,
    val listingUrl: String?,
) {
    fun toPrint() = Print(
        Files.prints.resolve(path).toAbsolutePath(),
        prompt,
        url ?: "",
        Theme.valueOf((theme ?: "Default").uppercase().replace(' ', '_')),
        title ?: "",
        description ?: "",
        previews?.map { preview -> Path(preview) } ?: emptyList(),
        previewUrls ?: emptyList(),
        thumbnail ?: "",
        sizeGuide ?: "",
        printFile ?: "",
        printFileUrl ?: "",
        listingUrl ?: "",
    )
}

data class BatchPrint(val url: String, val prompt: String) {
    fun toPrint(batch: String): Print {
        val logger = KotlinLogging.logger {}

        val output = Files.prints.resolve(batch).resolve("${UUID.nameUUIDFromBytes(url.toByteArray())}.png").toAbsolutePath()
        java.nio.file.Files.createDirectories(output.parent)

        if (output.exists()) {
            logger.info { "${output.fileName} was already downloaded." }
            return Print(output, prompt, url = url)
        }

        logger.info { "Downloading $url" }

        val (path: Path, duration: Duration) = measureTimedValue {
            val imageBytes = URI(url).toURL().readBytes()
            val image = ImmutableImage.loader().fromBytes(imageBytes)

            image.output(PngWriter(), output)
        }

        logger.info { "Downloading took ${duration.inWholeMilliseconds} ms" }

        return Print(path, prompt, url = url)
    }
}
