package utility.files

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import java.nio.file.Path
import java.nio.file.Paths

interface JsonMappable {
    fun toJson(): JsonObject
}

class Files {
    companion object {
        private val resources = Paths.get("src/main/resources")

        val backups: Path = resources.resolve("backups")

        val images: Path = resources.resolve("images")
        val frames: Path = images.resolve("frames")
        val previews: Path = images.resolve("previews")
        val prints: Path = images.resolve("prints")
        val thumbnails: Path = images.resolve("thumbnails")

        val social: Path = resources.resolve("social")

        fun <T> loadFromJson(json: Path): List<T> = Gson()
            .fromJson(json.toFile().bufferedReader().use { it.readText() }, object : TypeToken<List<T>>() {}.type)

        fun <T> storeAsJson(objects: List<T>, output: Path) where T : JsonMappable {
            val jsonContent = GsonBuilder().setPrettyPrinting().create().toJson(objects.map { it.toJson() })
            output.toFile().bufferedWriter().use { it.write(jsonContent) }
        }
    }
}