package prints

import model.Print
import utility.transformation.ImageUpscaler
import utility.transformation.upscaleWithRealESRGAN

class PrintFileCreator {
    private val upscaler = ImageUpscaler(upscaleWithRealESRGAN)

    fun create(print: Print): Print {
        val printFilePath = upscaler.upscale(print.path)
        return print.copy(printFile = printFilePath.toString())
    }
}