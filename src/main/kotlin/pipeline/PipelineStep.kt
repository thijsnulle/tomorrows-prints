package pipeline

import io.github.oshai.kotlinlogging.KotlinLogging
import model.Print
import utility.files.Files
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.measureTimedValue

abstract class PipelineStep {
    private val logger = KotlinLogging.logger {}
    private val backupPath = Files.backups.toAbsolutePath()
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    fun start(prints: List<Print>): List<Print> {
        if (prints.all { shouldSkip(it) }) {
            logger.info { "Skipping ${this::class.simpleName}" }
            return prints
        }

        logger.info { "Starting ${this::class.simpleName}" }

        val (processedPrints: List<Print>, duration: Duration) = measureTimedValue {
            prints.map { if (shouldSkip(it)) it else process(it) }
        }

        backup(processedPrints)

        logger.info { "Pipeline ${this::class.simpleName} took ${duration.inWholeMilliseconds} ms" }

        return processedPrints
    }

    abstract fun process(print: Print): Print
    abstract fun shouldSkip(print: Print): Boolean

    private fun backup(prints: List<Print>) {
        val fileName = "${dateFormatter.format(ZonedDateTime.now())}-${this::class.simpleName}.json"

        Files.storeAsJson(prints, backupPath.resolve(fileName).toAbsolutePath())
    }
}