package tmrw.pipeline.preview_upload

import com.google.cloud.storage.StorageOptions
import io.github.cdimascio.dotenv.dotenv
import tmrw.model.Print
import tmrw.pipeline.PipelineStep
import tmrw.pipeline.theme_allocation.Theme
import java.nio.file.Files
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

class PreviewUploadStep: PipelineStep(maximumThreads = 1) {

    private val bucketId = dotenv().get("GOOGLE_BUCKET_ID")
    private val bucket = StorageOptions.getDefaultInstance().service.get(bucketId)
        ?: error("Bucket $bucketId does not exist or you have not setup the correct credentials.")

    override fun process(print: Print): Print {
        require(print.previews.isNotEmpty()) { "${print.path.name} should have a previews associated with it." }
        require(print.theme != Theme.DEFAULT) { "${print.path.name} should have a theme associated with it." }

        val folderName = "${print.theme.value}/${print.path.nameWithoutExtension}"
        val previewUrls = print.previews.map { preview ->
            val fileName = "$folderName/${preview.fileName}"
            bucket.create(fileName, Files.readAllBytes(preview), "image/png")

            "https://storage.googleapis.com/$bucketId/$fileName"
        }

        return print.copy(previewUrls = previewUrls)
    }

    override fun shouldSkip(print: Print): Boolean = print.previewUrls.isNotEmpty()

}