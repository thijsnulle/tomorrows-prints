package social.pinterest

import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.openqa.selenium.chrome.ChromeDriver
import social.*
import java.time.Duration
import kotlin.math.max
import kotlin.time.measureTime
import kotlin.time.toKotlinDuration

const val HOME_PAGE = "https://www.pinterest.com"
const val CREATE_PIN_PAGE = "https://www.pinterest.com/pin-creation-tool/"

data class PinContent(val prompt: String, val listing: String, val theme: String, val preview: String)

val TIME_BETWEEN_POSTS = Duration.ofMinutes(3).toKotlinDuration()

class PinterestInfluencer {

    private val driver = ChromeDriver()
    private val prompter = PinterestContentPrompter()
    private var isLoggedIn = false

    fun post(posts: List<PinContent>) {
        login()

        posts.forEach { post ->
            val timeItTookToPost = measureTime {
                // TODO: replace [link] in `content.description` with actual link to shop.
                val content = prompter.ask(post.prompt)

                createPin(content, post)
            }

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

        driver.sendKeys(preview, "//input[@aria-label='File Upload']")
        driver.sendKeys(title, "//input[@placeholder='Add a title']")
        driver.sendKeys(listing, "//input[@placeholder='Add a link']")

        driver.click("//div[@data-offset-key]")
        driver.sendKeys(description, "//div[@data-offset-key]")

        driver.click("//button[@data-test-id='board-dropdown-select-button']")
        driver.click("//div[@data-test-id='board-row-${theme} Posters']")

        runBlocking { delay(1000) }

        driver.click("//div[text()='Publish']")

        driver.url("pinterest.com/pin")
    }
}