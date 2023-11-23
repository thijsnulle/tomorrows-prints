package tmrw.model

import com.sksamuel.scrimage.pixels.Pixel
import java.awt.Color

data class HsbColour(val h: Int, val s: Int, val b: Int) {
    companion object {
        fun fromPixel(pixel: Pixel): HsbColour {
            val hsb = FloatArray(3)
            Color.RGBtoHSB(pixel.red(), pixel.green(), pixel.blue(), hsb)
            return HsbColour(
                (hsb[0] * 360).toInt(),
                (hsb[1] * 100).toInt(),
                (hsb[2] * 100).toInt()
            )
        }
    }

    fun toColour(): Colour {
        if (s <= 15) {
            if (b <= 25) return Colour.BLACK
            if (b <= 50) return Colour.DARK_GRAY
            if (b <= 75) return Colour.LIGHT_GRAY

            return Colour.WHITE
        }

        if (s <= 25 && h in 15..35) return Colour.NEUTRAL

        if (h <= 15) return Colour.RED
        if (h <= 40) return Colour.ORANGE
        if (h <= 60) return Colour.YELLOW
        if (h <= 140) return Colour.GREEN
        if (h <= 175) return Colour.CYAN
        if (h <= 250) {
            if (h >= 220 && b <= 50) return Colour.NAVY

            return Colour.BLUE
        }
        if (h <= 285) return Colour.PURPLE
        if (h <= 330) return Colour.PINK

        return Colour.RED
    }
}

enum class Colour(val value: String) {
    BLACK("Black"),
    BLUE("Blue"),
    CYAN("Cyan"),
    DARK_GRAY("Dark Gray"),
    GREEN("Green"),
    LIGHT_GRAY("Light Gray"),
    NAVY("Navy"),
    NEUTRAL("Neutral"),
    ORANGE("Orange"),
    PINK("Pink"),
    PURPLE("Purple"),
    RED("Red"),
    WHITE("White"),
    YELLOW("Yellow"),
}