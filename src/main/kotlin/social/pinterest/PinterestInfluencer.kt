package social.pinterest

import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import preview.Poster
import social.*
import java.nio.file.Path

const val HOME_PAGE = "https://www.pinterest.com"
const val CREATE_PIN_PAGE = "https://www.pinterest.com/pin-builder"
const val CREATE_IDEA_PIN_PAGE = "https://www.pinterest.com/idea-pin-builder"

// TODO: Add listing URL to this functionality
data class PinContent(
    val prompt: String,
    val preview: Path,
    val carouselImage: Path? = null
)

class PinterestInfluencer: Influencer {

    private val driver = ChromeDriver(ChromeOptions().addArguments("--log-level=3"))
    private val pinterestPromptHandler = PinterestPromptHandler()
    private var isLoggedIn = false

    override fun post(posters: List<Poster>) {
        login()

        val posts: List<PinContent> = posters.map { poster ->
            val pins = poster.previews.map { PinContent(poster.prompt, it, poster.path) }
            val ideaPins = poster.previews.map { PinContent(poster.prompt, it) }

            pins + ideaPins + PinContent(poster.prompt, poster.path)
        }.flatten().shuffled()

        posts.forEach { post ->
            val content = pinterestPromptHandler.ask(post.prompt)

            if (post.carouselImage != null) {
                createPin(content, "", post.preview, post.carouselImage)
            } else {
                createIdeaPin(content, "", post.preview)
            }

            runBlocking { delay(2000) }
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
        image: Path
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

        runBlocking { delay(1000) }

        driver.click("//div[text()='Publish']")

        runBlocking { delay(1000) }

        driver.invisible("//svg[@aria-label='Saving Pin...']")
    }

    private fun createIdeaPin(
        content: PinterestContent,
        url: String,
        previewUrl: Path,
    ) {
        driver.get(CREATE_IDEA_PIN_PAGE)

        driver.sendKeys(previewUrl, "//input[@aria-label='File Upload']")
        driver.sendKeys(content.title, "//input[@placeholder='Add a title']")
        driver.sendKeys(url, "//input[@placeholder='Add a link']")

        driver.click("//div[@data-offset-key]")
        driver.sendKeys(content.description, "//div[@data-offset-key]")

        driver.click("//button[@data-test-id='board-dropdown-select-button']")
        driver.click("//div[@data-test-id='board-row-Posters']")
        runBlocking { delay(1000) }

        driver.click("//div[text()='Publish']")

        driver.url("pinterest.com/pin")
    }
}