package pipeline.steps

import kotlinx.serialization.json.*
import pipeline.PipelineStep
import preview.Poster
import utility.http.PrintfulHttpHandler
import kotlin.io.path.nameWithoutExtension

class PrintfulStep: PipelineStep() {

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

    override fun process(poster: Poster): Poster {
            val body = createJsonBody(poster.path.nameWithoutExtension, poster.printFileUrl, poster.printFileUrl, poster.printFileUrl)

            httpHandler.post("https://api.printful.com/store/products", body)
            return poster
        }

    // TODO: Add isInStore field to json for check
    override fun shouldSkip(poster: Poster): Boolean {
        TODO("Not yet implemented")
    }
}

private data class PosterVariant(val id: Int, val price: Double)