package social

import org.openqa.selenium.By.ByXPath
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.ExpectedConditions.*
import org.openqa.selenium.support.ui.WebDriverWait
import java.nio.file.Path
import java.time.Duration

val timeout: Duration = Duration.ofSeconds(60)
val interval: Duration = Duration.ofMillis(100)

fun WebDriver.find(xpath: String): WebElement {
        val element = WebDriverWait(this, timeout, interval)
                .until(presenceOfElementLocated(ByXPath(xpath)))

        Actions(this).moveToElement(element).perform()

        return element
}

fun WebDriver.invisible(xpath: String): Boolean = WebDriverWait(this, timeout, interval)
        .until(invisibilityOfElementLocated(ByXPath(xpath)))

fun WebDriver.click(xpath: String) {
        val element = WebDriverWait(this, timeout, interval)
                .until(elementToBeClickable(ByXPath(xpath)))

        Actions(this).moveToElement(element).perform()

        return element.click()
}

fun WebDriver.url(url: String): Boolean = WebDriverWait(this, timeout).until(urlMatches(url))

fun WebDriver.sendKeys(keys: String, xpath: String) = this.find(xpath).sendKeys(keys)
fun WebDriver.sendKeys(path: Path, xpath: String) = this.sendKeys(path.toString(), xpath)