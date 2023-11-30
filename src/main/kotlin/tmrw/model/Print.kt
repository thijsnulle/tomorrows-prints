package tmrw.model

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import io.github.oshai.kotlinlogging.KotlinLogging
import tmrw.pipeline.shopify_upload.PrintVariant
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
    val error: String = "",
    val colours: List<Colour> = emptyList(),
) : JsonMappable, CsvMappable {
    constructor(fileName: String, prompt: String) : this(Files.prints.resolve(fileName), prompt)

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
        jsonObject.addProperty("listingUrl", listingUrl)
        jsonObject.addProperty("thumbnail", thumbnail)
        jsonObject.addProperty("sizeGuide", sizeGuide)
        jsonObject.addProperty("printFile", printFile)
        jsonObject.addProperty("printFileUrl", printFileUrl)
        jsonObject.addProperty("error", error)

        jsonObject.add("previews", JsonArray().also { previews.forEach { preview -> it.add(preview.toString()) }})
        jsonObject.add("previewUrls", JsonArray().also { previewUrls.forEach { previewUrl -> it.add(previewUrl) }})
        jsonObject.add("colours", JsonArray().also { colours.forEach { colour -> it.add(colour.value) }})

        return jsonObject
    }

    override fun toCsvRows(startDate: LocalDateTime, intervalInMinutes: Long): List<String> {
        val getTaggedTopics = { taggedTopics.shuffled().take(10).joinToString(",") }

        val csvRows = previewUrls.mapIndexed { index, previewUrl ->
            val publishDate = startDate.plusMinutes((index + 1) * intervalInMinutes)

            "\"$title [${index + 1}/${previewUrls.size}]\",$previewUrl,${theme.value},,\"${decorateDescription()}\",$listingUrl,$publishDate,\"${getTaggedTopics()}\""
        }

        return csvRows
    }

    private fun decorateDescription(): String {
        val hashtags = hashtags.shuffled().take(MAX_NUMBER_OF_HASHTAGS).joinToString(" ") { "#$it" }
        val callToAction = callToActions.shuffled().first()

        return "$description ${callToAction.replace("[link]", listingUrl)} $hashtags"
    }

    fun toShopifyJson(): JsonObject {
        val product = JsonObject()

        product.addProperty("vendor", "Tomorrow's Prints")
        product.addProperty("status", "active")
        product.addProperty("metafields_global_title_tag", "$title • Tomorrow's Prints")
        product.addProperty("metafields_global_description_tag", """
            Discover our exclusive collection of unique and custom-made posters at Tomorrow's Prints. Each poster is meticulously designed in-house, ensuring that no two are alike.
            Perfect for adding a touch of personality to any space and whether you're looking for a statement piece or something subtle, Tomorrow's Prints has a poster for everyone.
            Shop now and elevate your space with our one-of-a-kind posters. With Tomorrow's Prints, you're not just getting a poster; you're bringing a unique piece of art into your life.
        """.trimIndent())

        product.addProperty("title", title)
        product.addProperty("body_html", """
            <p><strong>$title</strong></p>

            <p>$description</p>
            
            <p>Immerse yourself in the world of unique, custom-made posters that reflect the essence of your style. Our posters are more than just art; they're an expression of your individuality, crafted with care and passion.</p>

            <ul>
              <li><strong>Paper Thickness:</strong> 10.3 mil (0.26 mm)</li>
              <li><strong>Paper Weight:</strong> 189 g/m²</li>
              <li><strong>Opacity:</strong> 94%</li>
              <li><strong>ISO Brightness:</strong> 104%</li>
              <li><strong>Source:</strong> Paper is responsibly sourced from Japan 🇯🇵</li>
            </ul>

            <p>Perfect for adding a touch of personality to any space, our posters range from 12"x16" to 24"x36", allowing you to choose the perfect size for your home or office.</p>
        """.trimIndent())
        product.addProperty("product_type", theme.value)
        product.addProperty("tags", colours.joinToString(",") { it.value })

        product.add("images", JsonArray()
            .also { it.add(JsonObject().also { obj -> obj.addProperty("src", previewUrls.random()) }) }
            .also { it.add(JsonObject().also { obj -> obj.addProperty("src", url) }) }
        )

        product.add("options", JsonArray().also { arr -> arr.add(JsonObject()
            .also { obj -> obj.addProperty("name", "Size") }
            .also { obj -> obj.addProperty("values", "[${PrintVariant.variants.joinToString(",") { it.size }}]") }
        )})

        product.add("variants", JsonArray().also { arr -> PrintVariant.variants.forEach { variant ->
            arr.add(JsonObject()
                .also { obj -> obj.addProperty("option1", variant.size) }
                .also { obj -> obj.addProperty("price", variant.price) }
            )}
        })

        return JsonObject().also { it.add("product", product) }
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
    val error: String?,
    val colours: List<String>?,
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
        error ?: "",
        colours?.map { colour -> Colour.valueOf(colour.uppercase().replace(' ', '_')) } ?: emptyList(),
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
