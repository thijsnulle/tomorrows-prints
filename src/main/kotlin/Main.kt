import pipeline.PipelineStep
import pipeline.steps.PreviewStep
import pipeline.steps.SocialMediaStep
import preview.Poster
import java.nio.file.Paths

fun main() {
    val posters = listOf(
        Poster(Paths.get("src/main/resources/images/posters").toAbsolutePath().resolve("coffee.png"), "a coffee pot and a cup of coffee on a yellow background, in the style of american modernism, werkstätte, dark cyan and gray, juxtaposed imagery, detailed shading, use of light and shadow, bold-graphic"),
        Poster(Paths.get("src/main/resources/images/posters").toAbsolutePath().resolve("house.png"), "an abstract square cover depicting a small home with stairs, in the style of sun-soaked colours, konstantinos parthenis, rendered in maya, john pawson, primary colors, site-specific works, laurent chehere"),
        Poster(Paths.get("src/main/resources/images/posters").toAbsolutePath().resolve("man.png"), "a painting of a person standing in a crowded area near a light house and giant orange disc, in the style of black and white grayscale, digital minimalism, evgeni gordiets, nebulous forms, niko henrichon, grainy, black and white photos, irregular forms"),
        Poster(Paths.get("src/main/resources/images/posters").toAbsolutePath().resolve("wave.png"), "an image of a poster for the song colors, in the style of minimalist lines, vintage poster style, carpetpunk, chalk art, dramatic diagonals, rainbowcore, unexpected fabric combinations"),
    )

    val pipeline: List<PipelineStep> = listOf(
        PreviewStep(),
        SocialMediaStep(),
    )

    pipeline.fold(posters) { current, step -> step.process(current) }
}