package prints

import model.Print
import utility.transformation.ImageUpscaler
import utility.transformation.upscaleWithRealESRGAN

class PrintFileCreator {
    private val upscaler = ImageUpscaler(upscaleWithRealESRGAN)

    fun create(print: Print): Print = print.copy(printFile = upscaler.upscale(print.path, 4000))
}