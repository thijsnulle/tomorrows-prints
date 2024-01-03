package tmrw.post_processing.video_preview_generation.preview_generator

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.ImmutableImageLoader
import com.sksamuel.scrimage.nio.JpegWriter
import io.github.oshai.kotlinlogging.KotlinLogging
import tmrw.model.Print
import tmrw.utils.Files.Companion.batchFolder
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.*
import kotlin.random.Random

abstract class VideoPreviewGenerator(val frameRate: Int, val prefix: String) {

    private val logger = KotlinLogging.logger {}
    protected val loader: ImmutableImageLoader = ImmutableImage.loader()
    protected val random = Random.Default
    protected val writer: JpegWriter = JpegWriter.compression(85).withProgressive(true)

    @OptIn(ExperimentalPathApi::class)
    fun generateVideoPreviews(prints: List<Print>): List<Path> {
        val temporaryDirectory = Files.createTempDirectory("")

        val videoPreviewsFolder = tmrw.utils.Files.previews.batchFolder(prints.first()).parent.resolve("videos").toAbsolutePath()
        val videoPreviews = videoPreviewsFolder
            .walk()
            .filter { it.extension == "mp4" }
            .filter { it.toString().contains(prefix) }
            .toList()

        val printsToProcess = prints.filterNot { print -> videoPreviews.any {
            it.toString().contains(print.path.nameWithoutExtension)
        }}.ifEmpty {
            return videoPreviews
        }

        val newVideoPreviews = generate(printsToProcess, temporaryDirectory, videoPreviewsFolder)
            .also { temporaryDirectory.deleteRecursively() }

        return videoPreviews + newVideoPreviews
    }

    protected fun save(inputFolder: Path, outputFolder: Path, frameRate: Int): Path {
        val output = outputFolder.resolve("$prefix-${UUID.randomUUID()}.mp4")
        val commandsToGenerateCompleteVideoPreview = listOf(
            "ffmpeg", "-y",
            "-framerate", frameRate.toString(),
            "-i", "%d.jpeg",
            "-c:v", "libx264",
            "-pix_fmt", "yuv420p",
            output.toString(),
        )

        ProcessBuilder(commandsToGenerateCompleteVideoPreview)
            .directory(inputFolder.toFile())
            .start()
            .waitFor()

        return output
    }

    protected fun progress(prints: List<Print>, index: Int) {
        logger.info { "${this::class.simpleName} [${index+1}/${prints.size}]" }
    }

    protected fun output(outputFolder: Path, print: Print): Path = outputFolder.resolve(print.path.nameWithoutExtension)
        .createDirectories().toAbsolutePath()

    abstract fun generate(prints: List<Print>, inputFolder: Path, outputFolder: Path): List<Path>
}
