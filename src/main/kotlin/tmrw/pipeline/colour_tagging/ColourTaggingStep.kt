package tmrw.pipeline.colour_tagging

import com.sksamuel.scrimage.ImmutableImage
import tmrw.model.Print
import tmrw.pipeline.PipelineStep
import kotlin.math.abs

const val NUM_OPTIONS = 10

class ColourTaggingStep: PipelineStep() {

    val loader = ImmutableImage.loader()

    override fun process(print: Print): Print {
        val image = loader.fromPath(print.path)
        val pixels = image.pixels().map { Triple(
            it.red() * NUM_OPTIONS / 255,
            it.green() * NUM_OPTIONS / 255,
            it.blue() * NUM_OPTIONS / 255
        )}

        val counts = pixels.groupingBy { it }.eachCount()
            .entries
            .sortedBy { it.value }
            .reversed()

        var red = 0
        var green = 0
        var blue = 0
        var purple = 0
        var yellow = 0
        var cyan = 0
        var orange = 0

        counts.forEach {
            val (r, g, b) = it.key
            val count = it.value

            if (r >= 2 * g && r >= 2 * b) red += count
            if (g >= 2 * r && g >= 2 * b) green += count
            if (b >= 2 * r && b > g) blue += count

            if (abs(r - b) <= 1 && (r > g || b > g)) purple += count
            if (abs(r - g) <= 1 && (r > b || g > b)) yellow += count
            if (abs(g - b) <= 1 && (g > r || b > r)) cyan += count

            if (r > 1.5 * g && g > b) orange += count
        }

        return print
    }

    // 4322

    override fun shouldSkip(print: Print): Boolean = false
}