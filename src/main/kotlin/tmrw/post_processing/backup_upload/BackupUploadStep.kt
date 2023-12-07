package tmrw.post_processing.backup_upload

import com.google.cloud.storage.StorageOptions
import com.google.gson.GsonBuilder
import io.github.cdimascio.dotenv.dotenv
import tmrw.model.Print
import tmrw.post_processing.PostProcessingAggregate
import tmrw.post_processing.PostProcessingStep
import tmrw.utils.Files
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class BackupUploadStep: PostProcessingStep() {

    private val bucketId = dotenv().get("GOOGLE_BUCKET_ID")
    private val bucket = StorageOptions.getDefaultInstance().service.get(bucketId)
        ?: error("Bucket $bucketId does not exist or you have not setup the correct credentials.")

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    private val gson = GsonBuilder().setPrettyPrinting().create()

    override fun process(prints: List<Print>, aggregate: PostProcessingAggregate): PostProcessingAggregate {
        val fileName = "backups/Backup ${dateFormatter.format(ZonedDateTime.now())}"
        val jsonContent = gson.toJson(prints.map { it.toJson() })

        bucket.create(fileName, jsonContent.toByteArray(), "application/json")

        return aggregate
    }

    private fun backup(prints: List<Print>, withErrors: Boolean = false) {
        val fileName = "Backup ${dateFormatter.format(ZonedDateTime.now())}.json"

        Files.storeAsJson(prints, if (withErrors) Files.errors.resolve(fileName) else Files.backups.resolve(fileName))


    }
}