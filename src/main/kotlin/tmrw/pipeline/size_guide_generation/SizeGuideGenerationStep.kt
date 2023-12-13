package tmrw.pipeline.size_guide_generation

import tmrw.model.Print
import tmrw.pipeline.PipelineStep

class SizeGuideGenerationStep: PipelineStep() {
    private val sizeGuideGenerator = SizeGuideGenerator()

    override fun process(print: Print): Print = print.copy(
        sizeGuide = sizeGuideGenerator.generateSizeGuide(print).toString()
    )

    override fun postProcess(prints: List<Print>) {}

    override fun shouldSkip(print: Print): Boolean = print.sizeGuide.isNotEmpty()
}