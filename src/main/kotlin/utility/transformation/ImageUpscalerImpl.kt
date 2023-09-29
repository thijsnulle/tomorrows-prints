package utility.transformation

import java.nio.file.Path
import java.nio.file.Paths

fun interface ImageUpscalerImpl {
    fun upscale(input: Path, output: Path): Path
}

val upscaleWithRealESRGAN = ImageUpscalerImpl { input, output ->
    val commands = listOf(
        "./realesrgan-ncnn-vulkan",
        "-i", input.toString(),
        "-o", output.toString(),
        "-n", "realesrgan-x4plus-anime"
    )

    ProcessBuilder(commands)
        .directory(Paths.get("src/main/resources/executables/upscaler").toFile())
        .inheritIO()
        .start()
        .waitFor()

    output
}