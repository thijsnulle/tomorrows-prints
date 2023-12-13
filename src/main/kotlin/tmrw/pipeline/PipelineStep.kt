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

abstract class PipelineStep(private val maximumThreads: Int = 10) {

    private val logger = KotlinLogging.logger {}
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    fun start(prints: List<Print>): List<Print> {
        System.gc()

        val className = this::class.simpleName

        if (prints.all { shouldSkip(it) }) {
            logger.info { "Skipping $className" }

            postProcess(prints)

            return prints
        }

        logger.info { "Starting $className" }

        val printsWithErrors = mutableListOf<Print>()

        val semaphore = Semaphore(maximumThreads)
        var counter = 0
        val (processedPrints: List<Print>, duration: Duration) = measureTimedValue {
            runBlocking {
                prints.map { print ->
                    async(Dispatchers.Default) {
                        semaphore.withPermit {
                            try {
                                val newPrint = if (shouldSkip(print)) print else process(print)
                                counter += 1
                                logger.info { "$className [$counter/${prints.size}]" }

                                newPrint
                            } catch (e: Exception) {
                                printsWithErrors.add(print.copy(error = "$e\n${e.stackTraceToString()}"))
                                null
                            }
                        }
                    }
                }.awaitAll().filterNotNull()
            }
        }

        backup(processedPrints)
        if (printsWithErrors.isNotEmpty()) backup(printsWithErrors, withErrors = true)

        postProcess(processedPrints)

        logger.info { "Pipeline $className took ${duration.inWholeMilliseconds} ms" }

        return processedPrints
    }

    abstract fun process(print: Print): Print
    abstract fun postProcess(prints: List<Print>)
    abstract fun shouldSkip(print: Print): Boolean

    private fun backup(prints: List<Print>, withErrors: Boolean = false) {
        val fileName = "${dateFormatter.format(ZonedDateTime.now())}-${this::class.simpleName}.json"

        Files.storeAsJson(prints, if (withErrors) Files.errors.resolve(fileName) else Files.backups.resolve(fileName))
    }
}