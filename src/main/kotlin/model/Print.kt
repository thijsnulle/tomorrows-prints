package model

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import theme.Theme
import utility.files.Files
import utility.files.JsonMappable
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.name

data class Print(
    val path: Path,
    val prompt: String,
    val theme: Theme = Theme.DEFAULT,
    val previews: List<Path> = emptyList(),
    val thumbnail: Path = Paths.get(""),
    val printFile: Path = Paths.get(""),
    val printFileUrl: String = "",
    val listingUrl: String = "",
) : JsonMappable {
    constructor(fileName: String, prompt: String): this(Files.prints.resolve(fileName), prompt)

    override fun toJson(): JsonObject {
        val jsonObject = JsonObject()

        jsonObject.addProperty("path", path.name)
        jsonObject.addProperty("prompt", prompt)
        jsonObject.addProperty("theme", theme.value)

        val previews = JsonArray()
        previews.forEach { preview -> previews.add(preview.toString()) }
        jsonObject.add("previews", previews)

        jsonObject.addProperty("thumbnail", thumbnail.toString())
        jsonObject.addProperty("printFile", printFile.toString())
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
    val printFile: String?,
    val printFileUrl: String?,
    val listingUrl: String?,
) {
    fun toPrint() = Print(
        Files.prints.resolve(path),
        prompt,
        if (theme == null) Theme.DEFAULT else Theme.valueOf(theme.uppercase()),
        previews?.map { preview -> Path(preview) } ?: emptyList(),
        Path(thumbnail ?: ""),
        Path(printFile ?: ""),
        printFileUrl ?: "",
        listingUrl ?: "",
    )
}

data class BatchPrint(val url: String, val prompt: String) {
    fun toPrint(): Print {
        val imageBytes = URI(url).toURL().readBytes()
        val image = ImmutableImage.loader().fromBytes(imageBytes)
        val path = image.output(PngWriter(), Files.prints.resolve("${UUID.randomUUID()}.png"))

        return Print(path, prompt)
    }
}