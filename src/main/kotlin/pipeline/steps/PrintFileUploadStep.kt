package pipeline.steps

import com.google.cloud.storage.StorageOptions
import model.Print
import pipeline.PipelineStep
import prints.BUCKET
import theme.Theme
import java.nio.file.Files
import kotlin.io.path.name

class PrintFileUploadStep: PipelineStep(maximumThreads = 1) {

    private val bucket = StorageOptions.getDefaultInstance().service.get(BUCKET)
        ?: error("Bucket $BUCKET does not exist or you have not setup the correct credentials.")

    override fun process(print: Print): Print {
        require(print.theme != Theme.DEFAULT) { "${print.path.name} should have a theme associated with it." }

        val fileName = "${print.theme.value}/${print.path.fileName}"

        bucket.create(fileName, Files.readAllBytes(print.printFile))

        // TODO: add Google Cloud Storage URL to .env file
        return print.copy(printFileUrl = "https://storage.googleapis.com/tomorrows-prints/$fileName")
    }

    override fun shouldSkip(print: Print): Boolean = print.printFileUrl.isNotEmpty()
}