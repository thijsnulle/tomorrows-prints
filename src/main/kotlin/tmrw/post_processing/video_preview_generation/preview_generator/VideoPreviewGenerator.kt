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
import kotlin.io.path.createDirectories
import kotlin.io.path.nameWithoutExtension
import kotlin.random.Random

abstract class VideoPreviewGenerator(val frameRate: Int, val prefix: String) {

    protected val logger = KotlinLogging.logger {}
    protected val loader: ImmutableImageLoader = ImmutableImage.loader()
    protected val random = Random.Default
    protected val writer: JpegWriter = JpegWriter.compression(85).withProgressive(true)

    fun generateVideoPreviews(prints: List<Print>): List<Path> {
        val temporaryDirectory = Files.createTempDirectory("")

        return generate(prints, temporaryDirectory)
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

    protected fun outputFolder(print: Print): Path = tmrw.utils.Files.previews.batchFolder(print).parent
        .resolve("videos").resolve(print.path.nameWithoutExtension).createDirectories().toAbsolutePath()

    abstract fun generate(prints: List<Print>, inputFolder: Path): List<Path>
}
