package tmrw.pipeline.printful_synchronisation

import fuel.*
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import tmrw.model.Print
import tmrw.pipeline.PipelineStep
import tmrw.pipeline.shopify_upload.PrintVariant

const val SYNC_DELAY = 10000L
const val NOT_FOUND_DELAY = 30000L
const val TOO_MANY_REQUESTS_DELAY = 60000L

class PrintfulSynchronisationStep: PipelineStep(maximumThreads = 1) {

    override fun process(print: Print): Print = print.copy(synchronised = sync(print))
    override fun postProcess(prints: List<Print>) {}
    override fun shouldSkip(print: Print): Boolean = print.synchronised

    private fun sync(print: Print, retry: Boolean = true): Boolean = runBlocking {
        val responses = print.variantIds.zip(PrintVariant.variants).map { (variantId, variant) ->
            Fuel.put(
                url = "https://api.printful.com/sync/variant/@${variantId}",
                headers = mapOf("Content-Type" to "application/json", "Authorization" to "Bearer ${dotenv().get("PRINTFUL_KEY")}"),
                body = """{
                    "id": "@$variantId",
                    "files": [{ "url": "${print.printFileUrl}" }],
                    "variant_id": "${variant.id}"
                }""".trimIndent()
            ).also { delay(if (it.statusCode == 404) NOT_FOUND_DELAY else SYNC_DELAY) }
        }

        if (responses.all { it.statusCode == 200 }) return@runBlocking true
        if (!retry) return@runBlocking false

        delay(if (responses.any { it.statusCode == 404 }) NOT_FOUND_DELAY else TOO_MANY_REQUESTS_DELAY)

        return@runBlocking sync(print, retry = false)
    }
}
