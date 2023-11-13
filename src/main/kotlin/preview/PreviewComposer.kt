package preview

import io.github.oshai.kotlinlogging.KotlinLogging
import model.Print
import utility.files.Files
import utility.files.Files.Companion.batchFolderWithoutExtension
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
