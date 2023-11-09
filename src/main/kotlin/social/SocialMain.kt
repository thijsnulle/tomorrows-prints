package social

import social.pinterest.PinterestInfluencer
import java.nio.file.Paths

fun main() {
    println("Input JSON for posting on socials:")

    val schedule = Paths.get(readln().ifEmpty { null } ?: "src/main/resources/social/schedule.json")

    PinterestInfluencer().post(schedule)
}
