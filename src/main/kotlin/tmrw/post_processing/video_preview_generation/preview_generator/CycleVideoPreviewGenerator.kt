package tmrw.post_processing.video_preview_generation.preview_generator

import tmrw.model.Print
import tmrw.pipeline.preview_generation.FramedPreviewGenerator
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.listDirectoryEntries

const val VIDEO_PREVIEW_CYCLE_SIZE = 25

class CycleVideoPreviewGenerator: VideoPreviewGenerator(frameRate = 5) {
    override fun generate(prints: List<Print>, inputFolder: Path): List<Path> {
        val outputFolder = outputFolder(prints)
        val previewGenerator = FramedPreviewGenerator(previewFolder = inputFolder, createSquarePreviews = true)

        val chunks = prints
            .shuffled()
            .chunked(VIDEO_PREVIEW_CYCLE_SIZE)

        return chunks.map { chunk ->
            if (chunk.size != VIDEO_PREVIEW_CYCLE_SIZE) return@map null

            val previews = chunk
                .map(previewGenerator::generate)
                .map { it.previews.random() }

            inputFolder.listDirectoryEntries("*.jpeg").forEach { it.deleteIfExists() }

            previews.forEachIndexed { index, preview ->
                Files.copy(preview, inputFolder.resolve("$index.jpeg"))
            }

            save(inputFolder, outputFolder, frameRate = frameRate)
        }.filterNotNull()
    }
}
