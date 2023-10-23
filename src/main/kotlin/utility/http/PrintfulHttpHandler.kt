package utility.http

import fuel.Fuel
import fuel.post
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking

class PrintfulHttpHandler: HttpHandler() {

    override fun post(url: String, body: String) {
        runBlocking {
            Fuel.post(
                url = url,
                headers = mapOf("Authorization" to "Bearer ${dotenv().get("PRINTFUL_KEY")}"),
                body = body
            )
        }
    }
}