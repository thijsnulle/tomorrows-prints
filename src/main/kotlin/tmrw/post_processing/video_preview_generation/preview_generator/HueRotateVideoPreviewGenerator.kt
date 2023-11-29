package tmrw.post_processing.video_preview_generation.preview_generator

import com.sksamuel.scrimage.ImmutableImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import tmrw.model.HsbColour
import tmrw.model.Print
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.nameWithoutExtension

const val VIDEO_PREVIEW_HUE_ROTATION_FRAME_COUNT = 60

class HueRotateVideoPreviewGenerator: VideoPreviewGenerator(frameRate = 30, prefix = "hue-rotate") {
    override fun generate(prints: List<Print>, inputFolder: Path): List<Path> = prints.mapIndexed { index, print ->
        val image = loader.fromPath(print.path)
        val hsbPixels = image.pixels().map(HsbColour::fromPixel)

        runBlocking {
            (0..VIDEO_PREVIEW_HUE_ROTATION_FRAME_COUNT).map { frame ->
                async(Dispatchers.Default) {
                    val pixels = hsbPixels.map { it.rotate(360 * frame / VIDEO_PREVIEW_HUE_ROTATION_FRAME_COUNT) }
                        .map(HsbColour::toPixel)

                    ImmutableImage.create(image.width, image.height, pixels.toTypedArray())
                        .output(writer, inputFolder.resolve("$frame.jpeg"))
                }
            }.awaitAll()
        }

        progress(prints, index)

        save(inputFolder, outputFolder(print), frameRate)
    }
}