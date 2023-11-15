package social.pinterest

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.openqa.selenium.By
import org.openqa.selenium.Keys
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

data class PinContent(val prompt: String, val listing: String, val board: String, val preview: String) : JsonMappable {
    override fun toJson(): JsonObject {
        val jsonObject = JsonObject()

        jsonObject.addProperty("prompt", prompt)
        jsonObject.addProperty("listing", listing)
        jsonObject.addProperty("board", board)
        jsonObject.addProperty("preview", preview)

        return jsonObject
    }
}

val TIME_BETWEEN_POSTS = Duration.ofMinutes(3).toKotlinDuration()

class PinterestInfluencer {

    private val driver = ChromeDriver()
    private val prompter = PinterestContentPrompter()
    private var isLoggedIn = false
    private val gson = GsonBuilder().setPrettyPrinting().create()

    private val taggedTopics = listOf(
        "architecture poster",
        "art deco interior",
        "bedroom",
        "bedroom interior",
        "diy gifts",
        "diy wall art",
        "fashion poster",
        "graphic poster",
        "graphic design poster",
        "home interior design",
        "interior design tips",
        "living room",
        "luxury interior design",
        "minimalist poster",
        "modern interior",
        "music poster",
        "poster print",
        "print design",
        "retro poster",
        "wall art",
    )

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
        val (_, listing, board, preview) = pinContent

        driver.get(CREATE_PIN_PAGE)

        driver.sendKeys(preview, "//input[@aria-label='File upload']")
        driver.sendKeys(title, "//input[@placeholder='Add a title']")
        driver.sendKeys(listing, "//input[@placeholder='Add a link']")

        driver.click("//div[@data-offset-key]")
        driver.sendKeys(description, "//div[@data-offset-key]")

        driver.click("//button[@data-test-id='board-dropdown-select-button']")
        driver.click("//div[@data-test-id='board-row-$board']")

        taggedTopics
            .shuffled()
            .take(10)
            .forEach {
                val element = driver.find("//input[@placeholder='Search for a tag']")
                element.sendKeys(Keys.COMMAND, "a")
                element.sendKeys(Keys.DELETE)

                driver.sendKeys(it, "//input[@placeholder='Search for a tag']", withDelay = true)

                try {
                    driver.findElement(By.xpath("//div[text() = '$it']/.."))
                } catch (_: Exception) {
                    return@forEach
                }

                driver.click("//div[text() = '$it']/..")
                runBlocking { delay(500) }
            }

        runBlocking { delay(1000) }

        driver.click("//div[text()='Publish']")

        driver.url("pinterest.com/pin")
    }

    private fun saveCurrentPostSchedule(scheduleJson: Path, pinContents: List<PinContent>, currentIndex: Int) {
        val pinContentsToSave = pinContents.drop(currentIndex + 1).map { it.toJson() }
        val content = gson.toJson(pinContentsToSave)

        scheduleJson.toFile().bufferedWriter().use { it.write(content) }
    }
}