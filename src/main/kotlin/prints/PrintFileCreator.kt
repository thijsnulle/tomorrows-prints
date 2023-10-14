package prints

import com.google.cloud.storage.StorageOptions
import preview.Poster
import utility.transformation.ImageUpscaler
import utility.transformation.upscaleWithRealESRGAN
import java.nio.file.Files

const val BUCKET = "tomorrows-prints"

class PrintFileCreator {
    private val upscaler = ImageUpscaler(upscaleWithRealESRGAN)
    private val bucket = StorageOptions.getDefaultInstance().service.get(BUCKET)
        ?: error("Bucket $BUCKET does not exist or you have not setup the correct credentials.")

    fun create(posters: List<Poster>): List<Poster> {
        posters.map {
            val upscaledPoster = upscaler.upscale(it.path)
            val fileName = "${it.theme.value}/${upscaledPoster.fileName}"

            bucket.create(fileName, Files.readAllBytes(upscaledPoster))

            it.copy(printFileUrl = "https://storage.googleapis.com/tomorrows-prints/$fileName")
        }

        return posters
    }
}