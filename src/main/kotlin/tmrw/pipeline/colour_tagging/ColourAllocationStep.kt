package tmrw.pipeline.colour_tagging

import com.sksamuel.scrimage.ImmutableImage
import tmrw.model.Colour
import tmrw.model.HsbColour
import tmrw.model.Print
import tmrw.pipeline.PipelineStep

const val MINIMUM_COLOUR_PERCENTAGE = 10

class ColourAllocationStep: PipelineStep() {

    companion object {
        fun getColours(image: ImmutableImage): Set<Colour> = image.pixels()
            .map(HsbColour::fromPixel)
            .map(HsbColour::toColour)
            .groupingBy { it }.eachCount()
            .entries
            .associate { it.key to it.value * 100 / image.pixels().size }
            .filter { it.value >= MINIMUM_COLOUR_PERCENTAGE }
            .keys
    }

    private val loader = ImmutableImage.loader()

    override fun process(print: Print): Print {
        val image = loader.fromPath(print.path)
        val colours = getColours(image)

        return print.copy(colours = colours.toList())
    }

    override fun shouldSkip(print: Print): Boolean = print.colours.isNotEmpty()
}
