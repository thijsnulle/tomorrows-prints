package tmrw.pipeline.preview_generation

import io.github.oshai.kotlinlogging.KotlinLogging
import tmrw.model.Print
import tmrw.utils.Files
import tmrw.utils.Files.Companion.batchFolderWithoutExtension
import kotlin.io.path.*

abstract class PreviewComposer {

    private val logger = KotlinLogging.logger {}

    fun composePreviewsFor(print: Print): Print {
        logger.info { "Generating previews for ${print.path.fileName}" }

        val directory = Files.previews.batchFolderWithoutExtension(print)
        if (directory.exists()) {
            logger.info { "Previews for ${print.path.fileName} already exist, returning existing previews." }

            return print.copy(previews = directory.listDirectoryEntries("*.png"))
        }

        directory.createDirectory()

        return compose(print)
    }

    abstract fun compose(print: Print): Print
}
