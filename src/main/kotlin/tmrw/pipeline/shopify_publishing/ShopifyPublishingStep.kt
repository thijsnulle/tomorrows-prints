package tmrw.pipeline.shopify_publishing

import fuel.Fuel
import fuel.put
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import tmrw.model.Print
import tmrw.pipeline.PipelineStep

class ShopifyPublishingStep: PipelineStep(maximumThreads = 1) {
    override fun process(print: Print): Print {
        publish(print)

        return print.copy(published = true)
    }
    override fun postProcess(prints: List<Print>) {}
    override fun shouldSkip(print: Print): Boolean = print.published

    private fun publish(print: Print) {
        val response = runBlocking { Fuel.put(
            url = "${dotenv().get("SHOPIFY_STORE")}/admin/api/2023-10/products/${print.productId}.json",
            headers = mapOf("Content-Type" to "application/json", "X-Shopify-Access-Token" to dotenv().get("SHOPIFY_KEY")),
            body = """{
                "product": { "id": "${print.productId}", "status": "active" }    
            }""".trimMargin()
        ).also { delay(500) }}

        if (response.statusCode != 200) throw IllegalArgumentException(response.body)
    }
}