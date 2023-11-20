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
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.time.Duration
import kotlin.time.measureTimedValue

data class Print(
    val path: Path,
    val prompt: String,
    val theme: Theme = Theme.DEFAULT,
    val title: String = "",
    val previews: List<Path> = emptyList(),
    val previewUrls: List<String> = emptyList(),
    val thumbnail: String = "",
    val sizeGuide: String = "",
    val printFile: String = "",
    val printFileUrl: String = "",
    val listingUrl: String = "",
) : JsonMappable, CsvMappable {
    constructor(fileName: String, prompt: String): this(Files.prints.resolve(fileName), prompt)

    // TODO: add test for this method
    override fun toJson(): JsonObject {
        val jsonObject = JsonObject()

        jsonObject.addProperty("path", "${path.parent.name}/${path.name}")
        jsonObject.addProperty("prompt", prompt)
        jsonObject.addProperty("theme", theme.value)
        jsonObject.addProperty("title", title)

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

    override fun toCsvRow(): String = previews.joinToString("\n") {
        "$title,${it},${theme.value},,#generateDescription,$listingUrl,#generatePublishDate,#keywords"
    }
}

data class JsonPrint(
    val path: String,
    val prompt: String,
    val theme: String?,
    val title: String?,
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
        Theme.valueOf((theme ?: "Default").replace(' ', '_').uppercase()),
        title ?: "",
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
            return Print(output, prompt)
        }

        logger.info { "Downloading $url" }

        val (path: Path, duration: Duration) = measureTimedValue {
            val imageBytes = URI(url).toURL().readBytes()
            val image = ImmutableImage.loader().fromBytes(imageBytes)

            image.output(PngWriter(), output)
        }

        logger.info { "Downloading took ${duration.inWholeMilliseconds} ms" }

        return Print(path, prompt)
    }
}
