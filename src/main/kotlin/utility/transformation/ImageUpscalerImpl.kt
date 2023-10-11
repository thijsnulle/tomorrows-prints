package utility.transformation

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name

fun interface ImageUpscalerImpl {
    fun upscale(input: Path, output: Path): Path
}

val upscaleWithRealESRGAN = ImageUpscalerImpl { input, output ->
    println("Upscaling ${input.name} to ${output.name}")

    val executable = if (System.getProperty("os.name") == "Mac OS X") "./realesrgan-ncnn-vulkan"
                     else "src/main/resources/executables/upscaler/realesrgan-ncnn-vulkan.exe"

    val commands = listOf(
        executable,
        "-i", input.toString(),
        "-o", output.toString(),
        "-n", "realesrgan-x4plus-anime"
    )

    ProcessBuilder(commands)
        .directory(Paths.get("src/main/resources/executables/upscaler").toFile())
        .start()
        .waitFor()

    output
}