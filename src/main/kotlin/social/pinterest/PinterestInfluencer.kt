package social.pinterest

import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.openqa.selenium.chrome.ChromeDriver
import preview.Poster
import social.*
import theme.Theme
import java.nio.file.Path
import java.time.Duration
import kotlin.math.max
import kotlin.time.measureTime
import kotlin.time.toKotlinDuration

const val HOME_PAGE = "https://www.pinterest.com"
const val CREATE_PIN_PAGE = "https://www.pinterest.com/pin-builder"
const val CREATE_IDEA_PIN_PAGE = "https://www.pinterest.com/idea-pin-builder"

// TODO: Add listing URL to this functionality
data class PinContent(
    val prompt: String,
    val preview: Path,
    val carouselImage: Path? = null,
    val theme: Theme,
)

val DURATION = Duration.ofMinutes(3).toKotlinDuration()

class PinterestInfluencer: Influencer {

    private val driver = ChromeDriver()
    private val prompter = PinterestContentPrompter()
    private var isLoggedIn = false

    override fun post(posters: List<Poster>) {
        login()

        val posts: List<PinContent> = posters.map { poster ->
            val pins = poster.previews.map { PinContent(poster.prompt, it, poster.path, poster.theme) }
            val ideaPins = poster.previews.map { PinContent(poster.prompt, it, theme=poster.theme) }

            pins + ideaPins + PinContent(poster.prompt, poster.path, theme=poster.theme)
        }.flatten().shuffled()

        posts.forEach { post ->
            val timeItTookToPost = measureTime {
                val content = prompter.ask(post.prompt)

                if (post.carouselImage != null) {
                    createPin(content, "", post.preview, post.carouselImage, post.theme)
                } else {
                    createIdeaPin(content, "", post.preview, post.theme)
                }
            }

            val delayDuration = max(0, DURATION.minus(timeItTookToPost).inWholeMilliseconds)
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

    private fun createPin(
        content: PinterestContent,
        url: String,
        preview: Path,
        image: Path,
        theme: Theme,
    ) {
        driver.get(CREATE_PIN_PAGE)

        driver.sendKeys(content.title, "//textarea[@placeholder='Add your title']")
        driver.sendKeys(url, "//textarea[@placeholder='Add a destination link']")

        driver.click("//div[@data-offset-key]")
        driver.sendKeys(content.description, "//div[@data-offset-key]")

        driver.click("//div[text()='Add alt text']")
        driver.sendKeys(content.altText, "//textarea[@placeholder='Explain what people can see in the Pin']")

        driver.sendKeys(preview, "//input[@type='file']")
        driver.click("//div[text()='Create carousel']")
        driver.sendKeys(image, "//input[@type='file']")

        driver.click("//button[@data-test-id='board-dropdown-select-button']")
        driver.click("//div[@data-test-id='board-row-${theme.value} Posters']")

        runBlocking { delay(1000) }

        driver.click("//div[text()='Publish']")

        runBlocking { delay(1000) }

        driver.invisible("//svg[@aria-label='Saving Pin...']")
    }

    private fun createIdeaPin(
        content: PinterestContent,
        url: String,
        preview: Path,
        theme: Theme,
    ) {
        driver.get(CREATE_IDEA_PIN_PAGE)

        driver.sendKeys(preview, "//input[@aria-label='File Upload']")
        driver.sendKeys(content.title, "//input[@placeholder='Add a title']")
        driver.sendKeys(url, "//input[@placeholder='Add a link']")

        driver.click("//div[@data-offset-key]")
        driver.sendKeys(content.description, "//div[@data-offset-key]")

        driver.click("//button[@data-test-id='board-dropdown-select-button']")
        driver.click("//div[@data-test-id='board-row-${theme.value} Posters']")

        runBlocking { delay(1000) }

        driver.click("//div[text()='Publish']")

        driver.url("pinterest.com/pin")
    }
}