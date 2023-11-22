package tmrw.pipeline.shopify_upload

import kotlinx.serialization.json.*
import model.Print
import pipeline.PipelineStep

class ShopifyUploadStep: PipelineStep() {

    private val httpHandler = ShopifyHttpHandler()

    fun createJsonBody(title: String, bodyHtml: String, productType: String, status: String, imageUrl: String, variants: List<ShopifyVariant>): JsonObject {

        fun buildVariantJson(size: String, price: Int): JsonObject = buildJsonObject {
            put("option1", size)
            put("price", price)
        }

        return buildJsonObject {
            putJsonObject("product") {
                put("title", title)
                put("body_html", bodyHtml)
                put("vendor", "Tomorrow's Prints")
                put("product_type", productType)
                put("status", status)
                put("tags", "ADD TAGS")
                put("metafields_global_title_tag", "SEO title")
                put("metafields_global_description_tag", "SEO description")
                putJsonArray("images") {
                    addJsonObject {
                        put("src", imageUrl)
                    }
                }
                putJsonArray("options")  {
                    val stringBuilder = StringBuilder()
                    stringBuilder.append("[")
                    variants.forEach { variant -> stringBuilder.append("\"${variant.size}\"") }
                    stringBuilder.append("]")

                    addJsonObject {
                        put("name", "size")
                        put("values", stringBuilder.toString())
                    }
                }
                putJsonArray("variants") {
                    variants.forEach { variant -> add(buildVariantJson(variant.size, variant.price))}
                }
            }
        }
    }

    override fun process(print: Print): Print {
        val variants = listOf(ShopifyVariant("20x30", 100))
        val body = createJsonBody("Poster", "A nice poster", "Abstract poster", "draft", print.printFileUrl, variants).toString()
        httpHandler.post(body)
        return print
    }

    override fun shouldSkip(print: Print): Boolean {
        TODO("Not yet implemented")
    }

}

data class ShopifyVariant(val size: String, val price: Int)