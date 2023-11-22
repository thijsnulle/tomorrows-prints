package tmrw.pipeline.preview_generation

import io.github.oshai.kotlinlogging.KotlinLogging
import tmrw.model.Print
import tmrw.utils.Files
import tmrw.utils.Files.Companion.batchFolderWithoutExtension
import kotlin.io.path.*

abstract class PreviewGenerator {

    private val logger = KotlinLogging.logger {}

    fun generatePreviewsFor(print: Print): Print {
        val directory = Files.previews.batchFolderWithoutExtension(print)

        if (directory.exists()) return print.copy(previews = directory.listDirectoryEntries("*"))

        directory.createDirectory()

        return generate(print)
    }

    abstract fun generate(print: Print): Print
}
