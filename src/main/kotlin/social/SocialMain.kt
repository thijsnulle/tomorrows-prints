package social

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import preview.Poster
import preview.PosterJsonObject
import social.pinterest.PinContent
import social.pinterest.PinterestInfluencer
import java.nio.file.Paths
import java.util.Collections
import kotlin.io.path.nameWithoutExtension

fun main() {
    println("Input JSON for posting on socials:")

    val input = Paths.get(readln().ifEmpty { null } ?: "src/main/resources/social/schedule.json").toAbsolutePath()
    val pinContents = Gson()
        .fromJson<List<PinContent>>(
            input.toFile().bufferedReader().use { it.readText() },
            object : TypeToken<List<PinContent>>() {}.type
        ).map { it }

    PinterestInfluencer().post(pinContents)
}
