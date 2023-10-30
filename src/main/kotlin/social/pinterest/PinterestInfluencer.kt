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

open class PostContent(
    open val prompt: String,
    open val listingUrl: String,
    open val theme: Theme,
    open val preview: Path
)

data class PinContent(
    override val prompt: String,
    override val listingUrl: String,
    override val theme: Theme,
    override val preview: Path,
    val carouselImage: Path
) : PostContent(prompt, listingUrl, theme, preview)

data class IdeaPinContent(
    override val prompt: String,
    override val listingUrl: String,
    override val theme: Theme,
    override val preview: Path,
) : PostContent(prompt, listingUrl, theme, preview)

val TIME_BETWEEN_POSTS = Duration.ofMinutes(3).toKotlinDuration()

class PinterestInfluencer: Influencer {

    private val driver = ChromeDriver()
    private val prompter = PinterestContentPrompter()
    private var isLoggedIn = false

    override fun post(posters: List<Poster>) {
        login()

        val posts: List<PostContent> = posters.map { poster ->
            val pins = poster.previews.map { PinContent(poster.prompt, poster.listingUrl, poster.theme, it, poster.path) }
            val ideaPins = poster.previews.map { IdeaPinContent(poster.prompt, poster.listingUrl, poster.theme, it) }

            pins + ideaPins + IdeaPinContent(poster.prompt, poster.listingUrl, poster.theme, poster.path)
        }.flatten().shuffled()

        posts.forEach { post ->
            val timeItTookToPost = measureTime {
                // TODO: replace [link] in `content.description` with actual link to shop.
                val content = prompter.ask(post.prompt)

                when (post) {
                    is PinContent -> createPin(content, post)
                    is IdeaPinContent -> createIdeaPin(content, post)
                }
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
        val (title, description, altText) = postContent
        val (_, listingUrl, theme, previewImage, carouselImage) = pinContent

        driver.get(CREATE_PIN_PAGE)

        driver.sendKeys(title, "//textarea[@placeholder='Add your title']")
        driver.sendKeys(listingUrl, "//textarea[@placeholder='Add a destination link']")

        driver.click("//div[@data-offset-key]")
        driver.sendKeys(description, "//div[@data-offset-key]")

        driver.click("//div[text()='Add alt text']")
        driver.sendKeys(altText, "//textarea[@placeholder='Explain what people can see in the Pin']")

        driver.sendKeys(previewImage, "//input[@type='file']")
        driver.click("//div[text()='Create carousel']")
        driver.sendKeys(carouselImage, "//input[@type='file']")

        driver.click("//button[@data-test-id='board-dropdown-select-button']")
        driver.click("//div[@data-test-id='board-row-${theme.value} Posters']")

        runBlocking { delay(1000) }

        driver.click("//div[text()='Publish']")

        runBlocking { delay(1000) }

        driver.invisible("//svg[@aria-label='Saving Pin...']")
    }

    private fun createIdeaPin(postContent: PinterestContent, ideaPinContent: IdeaPinContent) {
        val (title, description, _) = postContent
        val (_, listingUrl, theme, previewImage) = ideaPinContent

        driver.get(CREATE_IDEA_PIN_PAGE)

        driver.sendKeys(previewImage, "//input[@aria-label='File Upload']")
        driver.sendKeys(title, "//input[@placeholder='Add a title']")
        driver.sendKeys(listingUrl, "//input[@placeholder='Add a link']")

        driver.click("//div[@data-offset-key]")
        driver.sendKeys(description, "//div[@data-offset-key]")

        driver.click("//button[@data-test-id='board-dropdown-select-button']")
        driver.click("//div[@data-test-id='board-row-${theme.value} Posters']")

        runBlocking { delay(1000) }

        driver.click("//div[text()='Publish']")

        driver.url("pinterest.com/pin")
    }
}