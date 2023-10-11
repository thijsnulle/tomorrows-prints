package social.pinterest

import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import preview.Poster
import social.*
import java.nio.file.Path
import java.time.Duration

const val HOME_PAGE = "https://www.pinterest.com"
const val CREATE_PIN_PAGE = "https://www.pinterest.com/pin-builder"
const val CREATE_IDEA_PIN_PAGE = "https://www.pinterest.com/idea-pin-builder"

class PinterestInfluencer: Influencer {

    private val driver = ChromeDriver(ChromeOptions().addArguments("--log-level=3"))
    private var isLoggedIn = false

    override fun post(poster: Poster) {
        login()

        val promptHandler = PinterestPromptHandler()
        poster.previews.forEach { preview ->
            val pinContent = promptHandler.ask(poster.prompt)
            val ideaPinContent = promptHandler.ask(poster.prompt)

            createPin(pinContent.title, pinContent.description, pinContent.altText, "", preview, poster.path)
            runBlocking { delay(1000) }

            createIdeaPin(ideaPinContent.title, ideaPinContent.description, "", preview)
            runBlocking { delay(1000) }
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
        title: String,
        description: String,
        altText: String,
        url: String,
        previewUrl: Path,
        posterUrl: Path
    ) {
        driver.get(CREATE_PIN_PAGE)

        driver.sendKeys(title, "//textarea[@placeholder='Add your title']")
        driver.sendKeys(url, "//textarea[@placeholder='Add a destination link']")

        driver.click("//div[@data-offset-key]")
        driver.sendKeys(description, "//div[@data-offset-key]")

        driver.click("//div[text()='Add alt text']")
        driver.sendKeys(altText, "//textarea[@placeholder='Explain what people can see in the Pin']")

        driver.sendKeys(previewUrl, "//input[@type='file']")
        driver.click("//div[text()='Create carousel']")
        driver.sendKeys(posterUrl, "//input[@type='file']")

        runBlocking { delay(1000) }

        driver.click("//div[text()='Publish']")

        runBlocking { delay(1000) }

        driver.invisible("//svg[@aria-label='Saving Pin...']")
    }

    private fun createIdeaPin(
        title: String,
        description: String,
        url: String,
        previewUrl: Path,
    ) {
        driver.get(CREATE_IDEA_PIN_PAGE)

        driver.sendKeys(previewUrl, "//input[@aria-label='File Upload']")
        driver.sendKeys(title, "//input[@placeholder='Add a title']")
        driver.sendKeys(url, "//input[@placeholder='Add a link']")

        driver.click("//div[@data-offset-key]")
        driver.sendKeys(description, "//div[@data-offset-key]")

        driver.click("//button[@data-test-id='board-dropdown-select-button']")
        driver.click("//div[@data-test-id='board-row-Posters']")
        runBlocking { delay(1000) }

        driver.click("//div[text()='Publish']")

        driver.url("pinterest.com/pin")
    }
}