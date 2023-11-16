package tmrw.pipeline

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import tmrw.model.Print
import tmrw.utils.Files
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.measureTimedValue

abstract class PipelineStep(private val maximumThreads: Int = 8) {

    private val logger = KotlinLogging.logger {}
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    fun start(prints: List<Print>): List<Print> {
        if (prints.all { shouldSkip(it) }) {
            logger.info { "Skipping ${this::class.simpleName}" }
            return prints
        }

        logger.info { "Starting ${this::class.simpleName}" }

        val semaphore = Semaphore(maximumThreads)

        val (processedPrints: List<Print>, duration: Duration) = measureTimedValue {
            runBlocking {
                prints.map {
                    async(Dispatchers.Default) {
                        semaphore.withPermit {
                            if (shouldSkip(it)) it else process(it)
                        }
                    }
                }.awaitAll()
            }
        }

        backup(processedPrints)

        logger.info { "Pipeline ${this::class.simpleName} took ${duration.inWholeMilliseconds} ms" }

        return processedPrints
    }

    abstract fun process(print: Print): Print
    abstract fun shouldSkip(print: Print): Boolean

    private fun backup(prints: List<Print>) {
        val fileName = "${dateFormatter.format(ZonedDateTime.now())}-${this::class.simpleName}.json"

        Files.storeAsJson(prints, Files.backups.resolve(fileName))
    }
}