package preview

import java.nio.file.Path

data class Image(val path: Path, val prompt: String)
data class Previews(val previews: List<Path>, val image: Image)

interface PreviewComposer {
    fun compose(image: Image): Previews
}
