package tmrw.pipeline.shopify_publishing

import fuel.Fuel
import fuel.put
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking
import tmrw.model.Print
import tmrw.pipeline.PipelineStep

class ShopifyPublishingStep: PipelineStep() {
    override fun process(print: Print): Print = print.copy(published = publish(print))
    override fun postProcess(prints: List<Print>) {}
    override fun shouldSkip(print: Print): Boolean = print.published

    private fun publish(print: Print): Boolean {
        val response = runBlocking { Fuel.put(
            url = "${dotenv().get("SHOPIFY_STORE")}/admin/api/2023-10/products/${print.productId}.json",
            headers = mapOf("Content-Type" to "application/json", "X-Shopify-Access-Token" to dotenv().get("SHOPIFY_KEY")),
            body = """{
                "product": { "id": "${print.productId}", "status": "active" }    
            }""".trimMargin()
        )}

        if (response.statusCode != 200) throw IllegalArgumentException(response.body)

        return true
    }
}