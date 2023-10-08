package social

import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.delay
import org.openqa.selenium.By
import org.openqa.selenium.By.ById
import org.openqa.selenium.By.ByXPath
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import preview.Poster
import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.Path

const val HOME_PAGE = "https://www.pinterest.com"
const val CREATE_PIN_PAGE = "https://www.pinterest.com/pin-builder"
const val CREATE_IDEA_PIN_PAGE = "https://www.pinterest.com/idea-pin-builder"

class PinterestInfluencer {

    private val driver = ChromeDriver()
    private val timeout = Duration.ofSeconds(60)
    private val interval = Duration.ofMillis(100)

    suspend fun post(posters: List<Poster>) {
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

    private suspend fun createPin(
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
        delay(5000)

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

    private fun WebDriver.find(xpath: String): WebElement = WebDriverWait(this, timeout, interval)
            .until(ExpectedConditions.presenceOfElementLocated(ByXPath(xpath)))

    private fun WebDriver.invisible(xpath: String) = WebDriverWait(this, timeout, interval)
            .until(ExpectedConditions.invisibilityOfElementLocated(ByXPath(xpath)))

    private fun WebDriver.click(xpath: String) = WebDriverWait(this, timeout, interval)
            .until(ExpectedConditions.elementToBeClickable(ByXPath(xpath))).click()

    private fun WebDriver.url(url: String) = WebDriverWait(this, timeout)
        .until(ExpectedConditions.urlMatches(url))

    private fun WebDriver.sendKeys(keys: String, xpath: String) = this.find(xpath).sendKeys(keys)
    private fun WebDriver.sendKeys(path: Path, xpath: String) = this.sendKeys(path.toString(), xpath)
}