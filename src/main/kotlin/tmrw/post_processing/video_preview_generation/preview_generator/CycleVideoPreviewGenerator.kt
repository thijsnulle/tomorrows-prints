package tmrw.post_processing.video_preview_generation.preview_generator

import tmrw.model.Print
import tmrw.pipeline.preview_generation.FramedPreviewGenerator
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.listDirectoryEntries
import kotlin.math.max

const val VIDEO_PREVIEW_CYCLE_SIZE = 25

class CycleVideoPreviewGenerator: VideoPreviewGenerator(frameRate = 5, prefix = "cycle") {
    override fun generate(prints: List<Print>, inputFolder: Path): List<Path> {
        val previewGenerator = FramedPreviewGenerator(previewFolder = inputFolder, createSquarePreviews = true)

        val printsToSelectFrom = (0 .. (VIDEO_PREVIEW_CYCLE_SIZE / prints.size))
            .flatMap { prints }.take(max( VIDEO_PREVIEW_CYCLE_SIZE, prints.size))

        return prints.mapIndexed { index, print ->
            val previews = printsToSelectFrom
                .shuffled()
                .take(VIDEO_PREVIEW_CYCLE_SIZE)
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
