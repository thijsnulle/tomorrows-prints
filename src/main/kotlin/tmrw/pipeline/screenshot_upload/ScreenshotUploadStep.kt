package tmrw.pipeline.screenshot_upload

import com.google.cloud.storage.StorageOptions
import io.github.cdimascio.dotenv.dotenv
import tmrw.model.Print
import tmrw.pipeline.PipelineStep
import tmrw.pipeline.theme_allocation.Theme
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

class ScreenshotUploadStep: PipelineStep(maximumThreads = 1) {

    private val bucketId = dotenv().get("GOOGLE_BUCKET_ID")
    private val bucket = StorageOptions.getDefaultInstance().service.get(bucketId)
        ?: error("Bucket $bucketId does not exist or you have not setup the correct credentials.")

    override fun process(print: Print): Print {
        require(print.screenshot.isNotEmpty()) { "${print.path.name} should have a screenshot." }
        require(print.theme != Theme.DEFAULT) { "${print.path.name} should have a theme associated with it." }

        val fileName = "${print.theme.value}/screenshots/${print.path.nameWithoutExtension}.jpeg"

        val screenshotPath = Path(print.screenshot)
        bucket.create(fileName, Files.readAllBytes(screenshotPath), "image/jpeg")

        return print.copy(screenshotUrl = "https://storage.googleapis.com/$bucketId/$fileName")
    }

    override fun shouldSkip(print: Print): Boolean = print.screenshotUrl.isNotEmpty()
}