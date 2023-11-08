package social.pinterest

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.openqa.selenium.chrome.ChromeDriver
import social.*
import utility.files.Files
import utility.files.JsonMappable
import java.nio.file.Path
import java.time.Duration
import kotlin.math.max
import kotlin.time.measureTime
import kotlin.time.toKotlinDuration

const val HOME_PAGE = "https://www.pinterest.com"
const val CREATE_PIN_PAGE = "https://www.pinterest.com/pin-creation-tool/"

data class PinContent(val prompt: String, val listing: String, val theme: String, val preview: String) : JsonMappable {
    override fun toJson(): JsonObject {
        val jsonObject = JsonObject()

        jsonObject.addProperty("prompt", prompt)
        jsonObject.addProperty("listing", listing)
        jsonObject.addProperty("theme", theme)
        jsonObject.addProperty("preview", preview)

        return jsonObject
    }
}

val TIME_BETWEEN_POSTS = Duration.ofMinutes(1).toKotlinDuration()

class PinterestInfluencer {

    private val driver = ChromeDriver()
    private val prompter = PinterestContentPrompter()
    private var isLoggedIn = false

    private val taggedTopics = listOf(
        "art deco interior",
        "bedroom",
        "bedroom interior",
        "diy gifts",
        "diy wall art",
        "graphic poster",
        "living room",
        "minimalist poster",
        "retro poster",
        "wall art",
    )

    init {
        require(taggedTopics.size <= 10) { "Can only select up to 10 tagged topics." }
    }

    fun post(scheduleJson: Path) {
        val posts = Files.loadFromJson<PinContent>(scheduleJson)

        login()

        posts.forEachIndexed { i, post ->
            val timeItTookToPost = measureTime {
                // TODO: replace [link] in `content.description` with actual link to shop.
                val content = prompter.ask(post.prompt)

                createPin(content, post)
            }

            saveCurrentPostSchedule(scheduleJson, posts, i)
            if (i == posts.lastIndex) return

            val delayDuration = max(0, TIME_BETWEEN_POSTS.minus(timeItTookToPost).inWholeMilliseconds)
            runBlocking { delay(delayDuration) }
        }
    }

    private fun login() {
        if (isLoggedIn) return

        driver.get(HOME_PAGE)

        driver.click("//div[text()='Log in']")
        driver.sendKeys(dotenv().get("PINTEREST_EMAIL"), "//input[@id='email']")
        driver.sendKeys(dotenv().get("PINTEREST_PASSWORD"), "//input[@id='password']")
        driver.click("//button[@type='submit']")

        driver.url("pinterest.com/business/hub")

        isLoggedIn = true
    }

    private fun createPin(postContent: PinterestContent, pinContent: PinContent) {
        val (title, description, _) = postContent
        val (_, listing, theme, preview) = pinContent

        driver.get(CREATE_PIN_PAGE)

        driver.sendKeys(preview, "//input[@aria-label='File upload']")
        driver.sendKeys(title, "//input[@placeholder='Add a title']")
        driver.sendKeys(listing, "//input[@placeholder='Add a link']")

        driver.click("//div[@data-offset-key]")
        driver.sendKeys(description, "//div[@data-offset-key]")

        // TODO: add theme selection
        driver.click("//button[@data-test-id='board-dropdown-select-button']")
        driver.click("//div[@data-test-id='board-row-All Posters']")

        taggedTopics.forEach {
            driver.sendKeys(it, "//input[@placeholder='Search for a tag']", withDelay = true)

            driver.click("//div[text() = '$it']/..")
            runBlocking { delay(500) }
        }

        runBlocking { delay(1000) }

        driver.click("//div[text()='Publish']")

        driver.url("pinterest.com/pin")
    }

    private fun saveCurrentPostSchedule(scheduleJson: Path, pinContents: List<PinContent>, currentIndex: Int) {
        val pinContentsToSave = pinContents.drop(currentIndex + 1)

        val content = GsonBuilder().setPrettyPrinting().create().toJson(pinContentsToSave.map {
            val jsonObject = JsonObject()

            jsonObject.addProperty("prompt", it.prompt)
            jsonObject.addProperty("listing", it.listing)
            jsonObject.addProperty("theme", it.theme)
            jsonObject.addProperty("preview", it.preview)

            jsonObject
        })

        scheduleJson.toFile().bufferedWriter().use { it.write(content) }
    }
}