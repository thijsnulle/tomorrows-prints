package tmrw.post_processing.video_preview_upload

import com.google.cloud.storage.StorageOptions
import io.github.cdimascio.dotenv.dotenv
import tmrw.model.Print
import tmrw.post_processing.PostProcessingAggregate
import tmrw.post_processing.PostProcessingStep
import java.nio.file.Files

class VideoPreviewUploadStep(val batch: String): PostProcessingStep() {

    private val bucketId = dotenv().get("GOOGLE_BUCKET_ID")
    private val bucket = StorageOptions.getDefaultInstance().service.get(bucketId)
        ?: error("Bucket $bucketId does not exist or you have not setup the correct credentials.")

    override fun process(prints: List<Print>, aggregate: PostProcessingAggregate): PostProcessingAggregate {
        val videoPreviewUrls = aggregate.videoPreviews.map {
            val fileName = "videos/$batch/${it.parent.fileName}/${it.fileName}"

            bucket.get(fileName)?.let { blob ->
                return@map blob.mediaLink
            }

            bucket.create(fileName, Files.readAllBytes(it), "video/mp4").mediaLink
        }

        return aggregate.copy(videoPreviewUrls = videoPreviewUrls)
    }
}