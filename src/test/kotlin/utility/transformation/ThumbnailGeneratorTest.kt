package utility.transformation

import com.sksamuel.scrimage.nio.ImmutableImageLoader
import preview.Poster
import java.awt.Color
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertTrue

class ThumbnailGeneratorTest {

    private val generator = ThumbnailGenerator()
    private val loader = ImmutableImageLoader()

    @Test
    fun testGenerateSimpleThumbnail() {
        val poster = Poster(
            Paths.get("src/test/resources/images/posters/poster.png").toAbsolutePath(),
            ""
        )

        val thumbnailPath = generator.generateThumbnail(poster)
        val thumbnailImage = loader.fromPath(thumbnailPath)

        val cornerPixels = listOf(
            thumbnailImage.topLeftPixel(),
            thumbnailImage.topRightPixel(),
            thumbnailImage.bottomRightPixel(),
            thumbnailImage.bottomLeftPixel(),
        )

        assertTrue {
            cornerPixels.all { it.toColor().toAWT().equals(Color.decode("#A7C7E7")) }
        }
    }
}