package tmrw.pipeline.size_guide_upload

import com.google.cloud.storage.StorageOptions
import io.github.cdimascio.dotenv.dotenv
import tmrw.model.Print
import tmrw.pipeline.PipelineStep
import tmrw.pipeline.theme_allocation.Theme
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

class SizeGuideUploadStep: PipelineStep() {

    private val bucketId = dotenv().get("GOOGLE_BUCKET_ID")
    private val bucket = StorageOptions.getDefaultInstance().service.get(bucketId)
        ?: error("Bucket $bucketId does not exist or you have not setup the correct credentials.")

    override fun process(print: Print): Print {
        require(print.sizeGuide.isNotEmpty()) { "${print.path.name} should have a size guide." }
        require(print.theme != Theme.DEFAULT) { "${print.path.name} should have a theme associated with it." }

        val fileName = "${print.theme.value}/sizeGuide/${print.path.nameWithoutExtension}.jpeg"

        val sizeGuidePath = Path(print.sizeGuide)
        bucket.create(fileName, Files.readAllBytes(sizeGuidePath), "image/jpeg")

        return print.copy(sizeGuideUrl = "https://storage.googleapis.com/$bucketId/$fileName")
    }

    override fun postProcess(prints: List<Print>) {}

    override fun shouldSkip(print: Print): Boolean = print.sizeGuideUrl.isNotEmpty()
}