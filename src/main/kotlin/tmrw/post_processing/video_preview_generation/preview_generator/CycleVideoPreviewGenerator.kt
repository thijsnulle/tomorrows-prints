package tmrw.post_processing.video_preview_generation.preview_generator

import tmrw.model.Print
import tmrw.pipeline.preview_generation.FramedPreviewGenerator
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension

const val VIDEO_PREVIEW_CYCLE_SIZE = 25

class CycleVideoPreviewGenerator: VideoPreviewGenerator(frameRate = 5, prefix = "cycle") {
    override fun generate(prints: List<Print>, inputFolder: Path): List<Path> {
        val previewGenerator = FramedPreviewGenerator(previewFolder = inputFolder, createSquarePreviews = true)

        // TODO: figure out how to handle this
        if (prints.size < VIDEO_PREVIEW_CYCLE_SIZE) return emptyList()

        return prints.mapIndexed { index, print ->
            val previews = prints
                .shuffled()
                .take(VIDEO_PREVIEW_CAROUSEL_SIZE)
                .let { it + print }
                .map(previewGenerator::generate)
                .map { it.previews.random() }

            inputFolder.listDirectoryEntries("*.jpeg").forEach { it.deleteIfExists() }

            previews.forEachIndexed { i, preview ->
                Files.copy(preview, inputFolder.resolve("$i.jpeg"))
            }

            progress(prints, index)

            save(inputFolder, outputFolder(print), frameRate = frameRate)
        }
    }
}
