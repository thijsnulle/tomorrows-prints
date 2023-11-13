package social

import social.pinterest.PinterestInfluencer
import java.nio.file.Paths

fun main() {
    println("Input file:")

    val schedule = Paths.get(
        readln().ifEmpty { null } ?: throw IllegalArgumentException("JSON schedule should be supplied.")
    )

    PinterestInfluencer().post(schedule)
}
