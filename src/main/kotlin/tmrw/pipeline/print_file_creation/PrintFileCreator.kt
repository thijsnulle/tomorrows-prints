package tmrw.pipeline.print_file_creation

import tmrw.model.Print
import tmrw.pipeline.print_file_creation.ImageUpscaler
import tmrw.pipeline.print_file_creation.upscaleWithRealESRGAN

class PrintFileCreator {
    private val upscaler = ImageUpscaler(upscaleWithRealESRGAN)

    fun create(print: Print): Print {
        val printFilePath = upscaler.upscale(print.path)
        return print.copy(printFile = printFilePath.toString())
    }
}