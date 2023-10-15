import fuel.Fuel
import fuel.get
import fuel.post
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import pipeline.PipelineStep
import pipeline.steps.PreviewStep
import pipeline.steps.SocialMediaStep
import preview.Poster

fun main() {
//    val posters = listOf(
//        Poster("coffee.png", "a coffee pot and a cup of coffee on a yellow background, in the style of american modernism, werkstätte, dark cyan and gray, juxtaposed imagery, detailed shading, use of light and shadow, bold-graphic"),
//        Poster("house.png", "an abstract square cover depicting a small home with stairs, in the style of sun-soaked colours, konstantinos parthenis, rendered in maya, john pawson, primary colors, site-specific works, laurent chehere"),
//        Poster("man.png", "a painting of a person standing in a crowded area near a light house and giant orange disc, in the style of black and white grayscale, digital minimalism, evgeni gordiets, nebulous forms, niko henrichon, grainy, black and white photos, irregular forms"),
//        Poster("wave.png", "an image of a poster for the song colors, in the style of minimalist lines, vintage poster style, carpetpunk, chalk art, dramatic diagonals, rainbowcore, unexpected fabric combinations"),
//    )
//
//    val pipeline: List<PipelineStep> = listOf(
//        PreviewStep(),
//        SocialMediaStep(),
//    )
//
//    pipeline.fold(posters) { current, step -> step.process(current) }

    val json = create("Poster", "https://i.imgur.com/4ygNwWn.jpeg", "https://i.imgur.com/4ygNwWn.jpeg", "https://i.imgur.com/4ygNwWn.jpeg")

    runBlocking {
        val body = Fuel.post(
            url = "https://api.printful.com/store/products",
            headers = mapOf("Authorization" to "Bearer ${dotenv().get("PRINTFUL_KEY")}"),
            body = json.toString()
        ).body

        println(body)
    }
}

val variants = listOf(
    PosterVariant(1349, "Enhanced Matte Paper Poster 12″x16", 10.95),
    PosterVariant(3876, "Enhanced Matte Paper Poster 12″x18", 10.95),
    PosterVariant(1, "Enhanced Matte Paper Poster 18″x24", 12.50),
    PosterVariant(16365, "Enhanced Matte Paper Poster 20″x30", 14.25),
    PosterVariant(1, "Enhanced Matte Paper Poster 24″x36", 17.50)
)

fun create(name: String, thumbnail: String, printFile: String, preview: String): JsonObject {

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
    }
}

data class PosterVariant(val id: Int, val name: String, val price: Double)