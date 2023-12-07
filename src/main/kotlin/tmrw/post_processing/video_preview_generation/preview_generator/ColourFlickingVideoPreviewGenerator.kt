package tmrw.post_processing.video_preview_generation.preview_generator

import com.sksamuel.scrimage.ImmutableImage
import tmrw.model.Colour
import tmrw.model.HsbColour
import tmrw.model.Print
import tmrw.pipeline.colour_tagging.ColourAllocationStep
import java.nio.file.Path

const val VIDEO_PREVIEW_COLOUR_FLICKING_FRAME_COUNT = 60

class ColourFlickingVideoPreviewGenerator: VideoPreviewGenerator(frameRate = 12, prefix = "colour-flicking") {
    override fun generate(prints: List<Print>, inputFolder: Path): List<Path> = prints.map { print ->
        val image = loader.fromPath(print.path)
        val colours = ColourAllocationStep.getColours(image)

        val frames = colours.map { colour -> getFrame(image, colour) }
        (0..VIDEO_PREVIEW_COLOUR_FLICKING_FRAME_COUNT).map { frameIndex ->
            frames.random().output(writer, inputFolder.resolve("$frameIndex.jpeg"))
        }

        save(inputFolder, outputFolder(print), frameRate)
    }

    private fun getFrame(image: ImmutableImage, colour: Colour): ImmutableImage {
        val hsbPixels = image.pixels().map(HsbColour::fromPixel)
        val newPixels = hsbPixels.map { if (it.toColour() == colour) it.toPixel() else it.copy(s = 0).toPixel() }

        return ImmutableImage.create(image.width, image.height, newPixels.toTypedArray())
    }
}