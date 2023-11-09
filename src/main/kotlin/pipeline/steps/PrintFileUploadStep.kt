package pipeline.steps

import com.google.cloud.storage.StorageOptions
import io.github.cdimascio.dotenv.dotenv
import model.Print
import pipeline.PipelineStep
import theme.Theme
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.name

class PrintFileUploadStep: PipelineStep(maximumThreads = 1) {

    private val bucketId = dotenv().get("GOOGLE_BUCKET_ID")
    private val bucket = StorageOptions.getDefaultInstance().service.get(bucketId)
        ?: error("Bucket $bucketId does not exist or you have not setup the correct credentials.")

    override fun process(print: Print): Print {
        require(print.theme != Theme.DEFAULT) { "${print.path.name} should have a theme associated with it." }

        val fileName = "${print.theme.value}/${print.path.fileName}"

        val printFilePath = Path(print.printFile)
        bucket.create(fileName, Files.readAllBytes(printFilePath))

        return print.copy(printFileUrl = "https://storage.googleapis.com/$bucketId/$fileName")
    }

    override fun shouldSkip(print: Print): Boolean = print.printFileUrl.isNotEmpty()
}