package tmrw.post_processing.video_preview_generation.preview_generator

import com.sksamuel.scrimage.ImmutableImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import tmrw.model.Colour
import tmrw.model.HsbColour
import tmrw.model.Print
import tmrw.pipeline.colour_tagging.ColourAllocationStep
import java.nio.file.Path

const val VIDEO_PREVIEW_COLOUR_ROTATION_FRAME_COUNT = 32

class ColourRotationVideoPreviewGenerator: VideoPreviewGenerator(frameRate = 8, prefix = "colour-rotation") {
    override fun generate(prints: List<Print>, inputFolder: Path, outputFolder: Path): List<Path> = prints.map { print ->
        val image = loader.fromPath(print.path)
        val colours = ColourAllocationStep.getColours(image, minimumPercentage = 5)

        val frames = colours.map { colour -> getFrame(image, colour) }
        val repeatedFrames = (0 until (VIDEO_PREVIEW_COLOUR_ROTATION_FRAME_COUNT / frames.size))
            .flatMap { frames }.take(VIDEO_PREVIEW_COLOUR_ROTATION_FRAME_COUNT)

        runBlocking {
            repeatedFrames.mapIndexed { frameIndex, frame ->
                async(Dispatchers.Default) {
                    frame.output(writer, inputFolder.resolve("$frameIndex.jpeg"))
                }
            }.awaitAll()
        }

        save(inputFolder, output(outputFolder, print), frameRate)
    }

    private fun getFrame(image: ImmutableImage, colour: Colour): ImmutableImage {
        val hsbPixels = image.pixels().map(HsbColour::fromPixel)
        val newPixels = hsbPixels.map { if (it.toColour() == colour) it.toPixel() else it.copy(s = 0).toPixel() }

        return ImmutableImage.create(image.width, image.height, newPixels.toTypedArray())
    }
}