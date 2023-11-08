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
    private val bucket = StorageOptions.getDefaultInstance().service.get(BUCKET)
        ?: error("Bucket $BUCKET does not exist or you have not setup the correct credentials.")

    fun create(print: Print): Print {
        require(print.theme != Theme.DEFAULT) { "${print.path.name} should have a theme associated with it." }

        val upscaledPoster = upscaler.upscale(print.path)
        val fileName = "${print.theme.value}/${print.path.fileName}"

        bucket.create(fileName, Files.readAllBytes(upscaledPoster))

        // TODO: add Google Cloud Storage URL to .env file
        return print.copy(printFileUrl = "https://storage.googleapis.com/tomorrows-prints/$fileName")
    }
}