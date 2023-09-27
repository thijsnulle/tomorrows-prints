package utility.transformation

import java.nio.file.Path
import java.nio.file.Paths

fun interface ImageUpscaler {
    fun upscale(input: Path, output: Path): Path
}

val upscaleWithRealESRGAN = ImageUpscaler { input, output ->
    // TODO: add support for windows
    val commands = listOf(
        "./realesrgan-ncnn-vulkan",
        "-i", input.toString(),
        "-o", output.toString(),
        // TODO: determine, based on prompt, which upscaler to use
        "-n", "realesrgan-x4plus-anime"
    )

    ProcessBuilder(commands)
        .directory(Paths.get("src/main/resources/executables/upscaler").toFile())
        .inheritIO()
        .start()
        .waitFor()

    output
}