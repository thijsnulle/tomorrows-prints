package tmrw.pipeline.shopify_upload

import fuel.Fuel
import fuel.post
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking
import tmrw.model.Print
import tmrw.pipeline.PipelineStep

data class PrintVariant(val size: String, val price: Int) {
    companion object {
        val variants = listOf(
            PrintVariant("12″×16″ • 30×40 cm", 20),
            PrintVariant("12″×18″ • 30×45 cm", 40),
            PrintVariant("18″×24″ • 45×60 cm", 60),
            PrintVariant("20″×30″ • 50×75 cm", 80),
            PrintVariant("24″×36″ • 60×90 cm", 100),
        )
    }
}

class ShopifyUploadStep: PipelineStep() {

    override fun process(print: Print): Print = print.copy(listingUrl = upload(print))
    override fun shouldSkip(print: Print): Boolean = print.listingUrl.isNotEmpty()

    private fun upload(print: Print): String {
        val result = runBlocking { Fuel.post(
            url = "${dotenv().get("SHOPIFY_STORE")}/admin/api/2023-10/products.json",
            headers = mapOf(
                "Content-Type" to "application/json",
                "X-Shopify-Access-Token" to dotenv().get("SHOPIFY_KEY")
            ),
            body = print.toShopifyJson().toString()
        )}

        if (result.statusCode != 201) throw IllegalArgumentException(result.body)

        val handleRegex = Regex("\"handle\":\"([^\"]+)\"")
        val handle = handleRegex.find(result.body)?.groups?.get(1)?.value ?: throw IllegalArgumentException()

        return "${dotenv().get("SHOPIFY_STORE")}/products/$handle"
    }
}
