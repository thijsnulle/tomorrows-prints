package deprecated

import kotlinx.serialization.json.*
import tmrw.model.Print
import tmrw.pipeline.PipelineStep
import kotlin.io.path.nameWithoutExtension

private data class PrintVariant(val id: Int, val price: Double)

class PrintfulStep: PipelineStep() {

    private val httpHandler = PrintfulHttpHandler()
    private val variants = listOf(
        PrintVariant(1349, 10.95),
        PrintVariant(3876, 10.95),
        PrintVariant(1, 12.50),
        PrintVariant(16365, 14.25),
        PrintVariant(1, 17.50)
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

    override fun process(print: Print): Print {
            val body = createJsonBody(print.path.nameWithoutExtension, print.printFileUrl, print.printFileUrl, print.printFileUrl)

            httpHandler.post("https://api.printful.com/store/products", body)
            return print
        }

    // TODO: Add isInStore field to json for check
    override fun shouldSkip(print: Print): Boolean = true
}