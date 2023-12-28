package tmrw.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import tmrw.model.Print
import java.io.FileOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

interface JsonMappable {
    fun toJson(): JsonObject
}

interface CsvMappable {
    fun toCsvRows(startDate: LocalDateTime, intervalInMinutes: Long): List<String>
}

class Files {
    companion object {
        private val resources = Paths.get("src/main/resources")
        private val gson = GsonBuilder().setPrettyPrinting().create()

        val backups: Path = resources.resolve("backups")
        val errors: Path = resources.resolve("errors")
        private val logs: Path = resources.resolve("logs")

        val images: Path = resources.resolve("images")
        val frames: Path = images.resolve("frames")
        val previews: Path = images.resolve("previews")
        val prints: Path = images.resolve("prints")
        val thumbnails: Path = images.resolve("thumbnails")
        val sizeGuides: Path = images.resolve("sizeGuides")
        val screenshots: Path = images.resolve("screenshots")

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

        fun enableLoggingToFile() {
            val logFile = logs.resolve("${LocalDateTime.now()}.log").toFile()
            val logFilePrintStream = FileOutputStream(logFile)

            val teeOutputStream = TeeOutputStream(System.out, logFilePrintStream)
            val printStream = PrintStream(teeOutputStream)

            System.setOut(printStream)
            System.setErr(printStream)

            Runtime.getRuntime().addShutdownHook(Thread {
                logFilePrintStream.close()
            })
        }
    }
}