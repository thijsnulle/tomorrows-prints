package pipeline.steps

import model.Print
import pipeline.PipelineStep
import utility.transformation.SizeGuideGenerator

class SizeGuideGenerationStep: PipelineStep() {
    private val sizeGuideGenerator = SizeGuideGenerator()

    override fun process(print: Print): Print = print.copy(
        sizeGuide = sizeGuideGenerator.generateSizeGuide(print).toString()
    )

    override fun shouldSkip(print: Print): Boolean = print.sizeGuide.isNotEmpty()
}