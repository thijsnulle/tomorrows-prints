package tmrw.post_processing.video_preview_generation.preview_generator

import tmrw.model.Print
import tmrw.pipeline.preview_generation.FramedPreviewGenerator
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.listDirectoryEntries

const val VIDEO_PREVIEW_CYCLE_SIZE = 50

class CycleVideoPreviewGenerator: VideoPreviewGenerator(frameRate = 5, prefix = "cycle") {
    override fun generate(prints: List<Print>, inputFolder: Path): List<Path> {
        val previewGenerator = FramedPreviewGenerator(previewFolder = inputFolder, createSquarePreviews = true)

        val printsToSelectFrom = (0 .. (VIDEO_PREVIEW_CYCLE_SIZE / prints.size))
            .flatMap { prints }.take(VIDEO_PREVIEW_CYCLE_SIZE)

        return prints.mapIndexed { index, print ->
            val previews = printsToSelectFrom
                .shuffled()
                .take(VIDEO_PREVIEW_CAROUSEL_SIZE)
                .let { it + print }
                .map(previewGenerator::generate)
                .map { it.random() }

            inputFolder.listDirectoryEntries("*.jpeg").forEach { it.deleteIfExists() }

            previews.forEachIndexed { i, preview ->
                Files.copy(preview, inputFolder.resolve("$i.jpeg"))
            }

            progress(prints, index)

            save(inputFolder, outputFolder(print), frameRate = frameRate)
        }
    }
}
