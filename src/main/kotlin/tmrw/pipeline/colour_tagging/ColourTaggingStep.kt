package tmrw.pipeline.colour_tagging

import com.sksamuel.scrimage.ImmutableImage
import tmrw.model.Colour
import tmrw.model.HsbColour
import tmrw.model.Print
import tmrw.pipeline.PipelineStep
import kotlin.math.abs
import kotlin.time.measureTime

const val MINIMUM_COLOUR_PERCENTAGE = 10

class ColourTaggingStep: PipelineStep() {

    private val loader = ImmutableImage.loader()

    override fun process(print: Print): Print {
        val image = loader.fromPath(print.path)

        val colours = image.pixels()
            .map(HsbColour::fromPixel)
            .map(HsbColour::toColour)
            .groupingBy { it }.eachCount()
            .entries
            .associate { it.key to it.value * 100 / image.pixels().size }
            .filter { it.value >= MINIMUM_COLOUR_PERCENTAGE }
            .keys

        return print.copy(colours = colours.toList())
    }

    override fun shouldSkip(print: Print): Boolean = false
}
