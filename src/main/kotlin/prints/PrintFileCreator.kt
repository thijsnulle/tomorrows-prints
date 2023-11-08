package prints

import com.google.cloud.storage.StorageOptions
import model.Print
import theme.Theme
import utility.transformation.ImageUpscaler
import utility.transformation.upscaleWithRealESRGAN
import java.nio.file.Files
import kotlin.io.path.name

const val BUCKET = "tomorrows-prints"

class PrintFileCreator {
    private val upscaler = ImageUpscaler(upscaleWithRealESRGAN)

    fun create(print: Print): Print = print.copy(printFile = upscaler.upscale(print.path, 4000))
}