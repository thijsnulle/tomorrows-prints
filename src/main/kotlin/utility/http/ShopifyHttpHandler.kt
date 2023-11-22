package utility.http

import fuel.Fuel
import fuel.post
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking

class ShopifyHttpHandler: HttpHandler() {

    override fun post(body: String) {
        runBlocking {
            val result = Fuel.post(
                url = "https://9b264c.myshopify.com/admin/api/2023-10/products.json",
                headers = mapOf(
                    "Content-Type" to "application/json",
                    "X-Shopify-Access-Token" to dotenv().get("SHOPIFY_KEY")
                ),
                body = body
            )

            println(result.statusCode)
        }
    }
}