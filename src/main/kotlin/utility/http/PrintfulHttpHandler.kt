package utility.http

import fuel.Fuel
import fuel.post
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking

class PrintfulHttpHandler: HttpHandler() {

    override fun post(body: String) {
        runBlocking {
            Fuel.post(
                url = "https://api.printful.com/store/products",
                headers = mapOf("Authorization" to "Bearer ${dotenv().get("PRINTFUL_KEY")}"),
                body = body
            )
        }
    }
}