package tmrw.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.openqa.selenium.By.ByXPath
import org.openqa.selenium.NotFoundException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.ExpectedConditions.*
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

val DEFAULT_TIMEOUT: Duration = Duration.ofSeconds(60)
val DEFAULT_INTERVAL: Duration = Duration.ofMillis(100)

fun WebDriver.find(
    xpath: String,
    timeout: Duration = DEFAULT_TIMEOUT,
    interval: Duration = DEFAULT_INTERVAL,
    retry: Boolean = true,
): WebElement {
    try {
        val element = WebDriverWait(this, timeout, interval).until(presenceOfElementLocated(ByXPath(xpath)))

        Actions(this).moveToElement(element).perform()

        return element
    } catch (_: Exception) {
        if (retry) return this.find(xpath, timeout, interval, false)
    }

    throw NotFoundException()
}

fun WebDriver.findQuick(xpath: String) = this.find(xpath, Duration.ofMillis(200), Duration.ofMillis(25))

fun WebDriver.click(
    xpath: String,
    timeout: Duration = DEFAULT_TIMEOUT,
    interval: Duration = DEFAULT_INTERVAL,
    retry: Boolean = true
) {
        try {
            val element = WebDriverWait(this, timeout, interval)
                .until(elementToBeClickable(ByXPath(xpath)))

            Actions(this).moveToElement(element).perform()
            element.click()
        } catch (_: Exception) {
            if (!retry) this.click(xpath, timeout, interval, false)
        }
}

fun WebDriver.url(url: String): Boolean = WebDriverWait(this, DEFAULT_TIMEOUT).until(urlMatches(url))

fun WebDriver.sendKey(
    key: Char,
    xpath: String,
    timeout: Duration = DEFAULT_TIMEOUT,
    interval: Duration = DEFAULT_INTERVAL,
) = this.find(xpath, timeout, interval).sendKeys(key.toString())

fun WebDriver.sendKeys(
    keys: String,
    xpath: String,
    timeout: Duration = DEFAULT_TIMEOUT,
    interval: Duration = DEFAULT_INTERVAL,
    withDelay: Boolean = false
) {
    if (!withDelay) {
        this.find(xpath, timeout, interval).sendKeys(keys)
        return
    }

    val element = this.find(xpath)
    keys.forEach {
        element.sendKeys("$it")
        runBlocking { delay(50) }
    }
}
