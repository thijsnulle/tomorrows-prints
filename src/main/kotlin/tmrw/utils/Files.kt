package tmrw.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import tmrw.model.Print
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

interface JsonMappable {
    fun toJson(): JsonObject
}

class Files {
    companion object {
        private val resources = Paths.get("src/main/resources")
        private val gson = GsonBuilder().setPrettyPrinting().create()

        val backups: Path = resources.resolve("backups")
        val errors: Path = resources.resolve("errors")
        val logs: Path = resources.resolve("logs")

        val images: Path = resources.resolve("images")
        val frames: Path = images.resolve("frames")
        val previews: Path = images.resolve("previews")
        val prints: Path = images.resolve("prints")
        val thumbnails: Path = images.resolve("thumbnails")
        val sizeGuides: Path = images.resolve("sizeGuides")

        val social: Path = resources.resolve("social")

        inline fun <reified T> loadFromJson(json: Path): List<T> = Gson()
            .fromJson(json.toAbsolutePath().toFile().bufferedReader().use { it.readText() }, object : TypeToken<List<T>>() {}.type)

        fun <T> storeAsJson(objects: List<T>, output: Path) where T : JsonMappable {
            val jsonContent = gson.toJson(objects.map { it.toJson() })
            output.toFile().bufferedWriter().use { it.write(jsonContent) }
        }

        fun Path.batchFolder(print: Print): Path {
            val folder = this.resolve(print.path.parent.nameWithoutExtension)

            Files.createDirectories(folder)

            return folder.resolve(print.path.name)
        }

        fun Path.batchFolderWithoutExtension(print: Print): Path {
            val folder = this.resolve(print.path.parent.nameWithoutExtension)

            Files.createDirectories(folder)

            return folder.resolve(print.path.nameWithoutExtension)
        }
    }
}