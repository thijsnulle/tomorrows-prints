package tmrw.pipeline.print_file_generation

import tmrw.model.Print

class PrintFileGenerator {
    private val upscaler = ImageUpscaler(upscaleWithRealESRGAN)

    fun generate(print: Print): Print {
        val printFilePath = upscaler.upscale(print.path)
        return print.copy(printFile = printFilePath.toString())
    }
}