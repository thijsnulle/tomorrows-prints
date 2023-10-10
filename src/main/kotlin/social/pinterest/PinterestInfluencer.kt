package social.pinterest

import io.github.cdimascio.dotenv.dotenv
import org.openqa.selenium.chrome.ChromeDriver
import preview.Poster
import social.*
import java.nio.file.Path

const val HOME_PAGE = "https://www.pinterest.com"
const val CREATE_PIN_PAGE = "https://www.pinterest.com/pin-builder"
const val CREATE_IDEA_PIN_PAGE = "https://www.pinterest.com/idea-pin-builder"

class PinterestInfluencer {

    private val driver = ChromeDriver()

    fun post(poster: Poster) {
        // TODO: implement method of what content to post per poster
    }

    private fun login() {
        driver.get(HOME_PAGE)

        driver.click("//div[text()='Log in']")
        driver.sendKeys(dotenv().get("PINTEREST_EMAIL"), "//input[@id='email']")
        driver.sendKeys(dotenv().get("PINTEREST_PASSWORD"), "//input[@id='password']")
        driver.click("//button[@type='submit']")

        driver.url("pinterest.com/business/hub")
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

        driver.click("//div[text()='Publish']")

        driver.find("//svg[@aria-label='Saving Pin...']")
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
        driver.click("//div[text()='Publish']")

        driver.url("pinterest.com/pin")
    }
}