package tmrw.pipeline.print_file_upload

import com.google.cloud.storage.StorageOptions
import io.github.cdimascio.dotenv.dotenv
import tmrw.model.Print
import tmrw.pipeline.PipelineStep
import tmrw.pipeline.theme_allocation.Theme
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.name

class PrintFileUploadStep: PipelineStep() {

    private val bucketId = dotenv().get("GOOGLE_BUCKET_ID")
    private val bucket = StorageOptions.getDefaultInstance().service.get(bucketId)
        ?: error("Bucket $bucketId does not exist or you have not setup the correct credentials.")

    override fun process(print: Print): Print {
        require(print.printFile.isNotEmpty()) { "${print.path.name} should have a print file." }
        require(print.theme != Theme.DEFAULT) { "${print.path.name} should have a theme associated with it." }

        val fileName = "${print.theme.value}/print-files/${print.path.fileName}"

        val printFilePath = Path(print.printFile)
        bucket.create(fileName, Files.readAllBytes(printFilePath), "image/jpeg")

        return print.copy(printFileUrl = "https://storage.googleapis.com/$bucketId/$fileName")
    }

    override fun postProcess(prints: List<Print>) {}

    override fun shouldSkip(print: Print): Boolean = print.printFileUrl.isNotEmpty()
}