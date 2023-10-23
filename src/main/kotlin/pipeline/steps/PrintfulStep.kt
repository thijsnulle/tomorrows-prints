package pipeline.steps

import io.github.cdimascio.dotenv.dotenv
import kotlinx.serialization.json.*
import pipeline.PipelineStep
import preview.Poster
import utility.http.HttpHandler
import utility.http.PrintfulHttpHandler

class PrintfulStep: PipelineStep {

    private val httpHandler = PrintfulHttpHandler()
    private val variants = listOf(
        PosterVariant(1349, 10.95),
        PosterVariant(3876, 10.95),
        PosterVariant(1, 12.50),
        PosterVariant(16365, 14.25),
        PosterVariant(1, 17.50)
    )

    private fun createJsonBody(name: String, thumbnail: String, printFile: String, preview: String): String {

        fun syncVariant(variantId: Int, retailPrice: Double): JsonObject = buildJsonObject {
            put("variant_id", variantId)
            put("retail_price", retailPrice)
            putJsonArray("files") {
                add(buildJsonObject {
                    put("url", printFile)
                })
                add(buildJsonObject {
                    put("type", "preview")
                    put("url", preview)
                })
            }
        }

        return buildJsonObject {
            putJsonObject("sync_product") {
                put("name", name)
                put("thumbnail", thumbnail)
            }
            putJsonArray("sync_variants") {
                variants.forEach { variant -> add(syncVariant(variant.id, variant.price * 2)) }
            }
        }.toString()
    }

    override fun process(posters: List<Poster>): List<Poster> {
        return posters.map {
            val body = createJsonBody(
                "Poster",
                "https://i.imgur.com/4ygNwWn.jpeg",
                "https://i.imgur.com/4ygNwWn.jpeg",
                "https://i.imgur.com/4ygNwWn.jpeg"
            )
            httpHandler.post(body)
            it
        }
    }

}

private data class PosterVariant(val id: Int, val price: Double)