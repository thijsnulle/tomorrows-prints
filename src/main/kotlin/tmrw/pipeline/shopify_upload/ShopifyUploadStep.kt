package tmrw.pipeline.shopify_upload

import fuel.Fuel
import fuel.HttpResponse
import fuel.post
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking
import tmrw.model.Print
import tmrw.pipeline.PipelineStep

data class PrintVariant(val id: Int, val size: String, val price: Int) {
    companion object {
        val variants = listOf(
            PrintVariant(1349, "12″×16″ • 30×40 cm", 20),
            PrintVariant(3876, "12″×18″ • 30×45 cm", 40),
            PrintVariant(1, "18″×24″ • 45×60 cm", 60),
            PrintVariant(16365, "20″×30″ • 50×75 cm", 80),
            PrintVariant(2, "24″×36″ • 60×90 cm", 100),
        )
    }
}

class ShopifyUploadStep: PipelineStep() {

    override fun process(print: Print): Print {
        val response = upload(print)

        return print.copy(
            productId = extractProductId(response.body),
            listingUrl = extractListingUrl(response.body),
            variantIds = extractVariantIds(response.body),
        )
    }

    override fun shouldSkip(print: Print): Boolean = print.productId.isNotEmpty()

    private fun upload(print: Print): HttpResponse {
        val response = runBlocking { Fuel.post(
            url = "${dotenv().get("SHOPIFY_STORE")}/admin/api/2023-10/products.json",
            headers = mapOf(
                "Content-Type" to "application/json",
                "X-Shopify-Access-Token" to dotenv().get("SHOPIFY_KEY")
            ),
            body = print.toShopifyJson().toString()
        )}

        if (response.statusCode != 201) throw IllegalArgumentException(response.body)

        return response
    }

    private fun extractProductId(content: String): String = Regex("\"product\":\\{\"id\":(\\d+),")
        .find(content)?.groups?.get(1)?.value ?: throw IllegalArgumentException()

    private fun extractListingUrl(content: String): String {
        val handle = Regex("\"handle\":\"([^\"]+)\"")
            .find(content)?.groups?.get(1)?.value ?: throw IllegalArgumentException()

        return "${dotenv().get("SHOPIFY_STORE")}/products/$handle"
    }

    private fun extractVariantIds(content: String): List<String> = Regex("\"id\":(\\d+),\"product_id\":\\d+,\"title\"")
        .findAll(content).map { it.groups[1]?.value ?: throw IllegalArgumentException() }.toList()
}
