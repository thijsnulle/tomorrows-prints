package social

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import preview.PosterJsonObject
import social.pinterest.PinterestInfluencer
import java.nio.file.Paths

fun socialMain() {
    println("Input JSON for posting on socials:")

    // TODO: move to `PosterUtils` class or add to `Poster` data class
    val input = Paths.get(readln().ifEmpty { null } ?: "src/main/resources/default.json").toAbsolutePath()
    val posters = Gson()
        .fromJson<List<PosterJsonObject>>(
            input.toFile().bufferedReader().use { it.readText() },
            object : TypeToken<List<PosterJsonObject>>() {}.type
        ).map { it.toPoster() }

    val socialMediaInput: [

    val influencer = PinterestInfluencer()
}