package tmrw.pipeline.thumbnail_upload

import com.google.cloud.storage.StorageOptions
import io.github.cdimascio.dotenv.dotenv
import tmrw.model.Print
import tmrw.pipeline.PipelineStep
import tmrw.pipeline.theme_allocation.Theme
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

class ThumbnailUploadStep: PipelineStep() {

    private val bucketId = dotenv().get("GOOGLE_BUCKET_ID")
    private val bucket = StorageOptions.getDefaultInstance().service.get(bucketId)
        ?: error("Bucket $bucketId does not exist or you have not setup the correct credentials.")

    override fun process(print: Print): Print {
        require(print.thumbnail.isNotEmpty()) { "${print.path.name} should have a thumbnail." }
        require(print.theme != Theme.DEFAULT) { "${print.path.name} should have a theme associated with it." }

        val fileName = "${print.theme.value}/thumbnails/${print.path.nameWithoutExtension}.jpeg"

        val thumbnailPath = Path(print.thumbnail)
        bucket.create(fileName, Files.readAllBytes(thumbnailPath), "image/jpeg")

        return print.copy(thumbnailUrl = "https://storage.googleapis.com/$bucketId/$fileName")
    }

    override fun shouldSkip(print: Print): Boolean = print.thumbnailUrl.isNotEmpty()
}