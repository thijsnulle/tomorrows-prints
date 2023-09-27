import utility.prompt.TemplatePromptHandler
import utility.transformation.ImageTransformer
import utility.transformation.upscaleWithRealESRGAN
import java.nio.file.Paths

suspend fun main(args: Array<String>) {
    val transformer = ImageTransformer(upscaleWithRealESRGAN)

    val input = Paths.get("src/main/resources/images/input.png").toAbsolutePath()
    val output = Paths.get("src/main/resources/images/output.png").toAbsolutePath()

    transformer.upscale(input, output)
}