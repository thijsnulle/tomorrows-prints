package model

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import io.github.oshai.kotlinlogging.KotlinLogging
import theme.Theme
import utility.files.Files
import utility.files.JsonMappable
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
    val previews: List<Path> = emptyList(),
    val thumbnail: String = "",
    val sizeGuide: String = "",
    val printFile: String = "",
    val printFileUrl: String = "",
    val listingUrl: String = "",
) : JsonMappable {
    constructor(fileName: String, prompt: String): this(Files.prints.resolve(fileName), prompt)

    override fun toJson(): JsonObject {
        val jsonObject = JsonObject()

        jsonObject.addProperty("path", "${path.parent.name}/${path.name}")
        jsonObject.addProperty("prompt", prompt)
        jsonObject.addProperty("theme", theme.value)

        val previews = JsonArray()
        this.previews.forEach {
            preview -> previews.add(preview.toString())
        }
        jsonObject.add("previews", previews)

        jsonObject.addProperty("thumbnail", thumbnail)
        jsonObject.addProperty("sizeGuide", sizeGuide)
        jsonObject.addProperty("printFile", printFile)
        jsonObject.addProperty("printFileUrl", printFileUrl)

        return jsonObject
    }
}

data class JsonPrint(
    val path: String,
    val prompt: String,
    val theme: String?,
    val previews: List<String>?,
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
        previews?.map { preview -> Path(preview) } ?: emptyList(),
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
