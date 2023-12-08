package tmrw.pipeline.screenshot_generation

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.JpegWriter
import tmrw.model.Print
import tmrw.pipeline.PipelineStep
import tmrw.utils.Files
import tmrw.utils.Files.Companion.batchFolderWithoutExtension
import kotlin.io.path.name

const val SCREENSHOT_FACTOR = 0.33

class ScreenshotGenerationStep: PipelineStep(maximumThreads = 4) {

    private val loader = ImmutableImage.loader()
    private val writer = JpegWriter.compression(85).withProgressive(true)

    override fun process(print: Print): Print {
        require(print.printFile.isNotEmpty()) { "${print.path.name} should have a print file to create a screenshot." }

        val output = Files.screenshots.batchFolderWithoutExtension(print)
            .let { it.resolveSibling("${it.fileName}.jpeg") }
        val screenshot = loader.fromFile(print.printFile)
            .resize(SCREENSHOT_FACTOR)
            .output(writer, output)

        return print.copy(screenshot = screenshot.toString())
    }

    override fun shouldSkip(print: Print): Boolean = print.screenshot.isNotEmpty()
}