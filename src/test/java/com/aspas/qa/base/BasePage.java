package com.aspas.qa.base;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    // Longer wait for slow async API responses from localhost:8080
    protected WebDriverWait longWait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait     = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    /**
     * JS-based click — bypasses z-index overlay interceptions.
     * Use this when a toast or modal overlay would intercept a normal click.
     */
    protected void jsClick(By locator) {
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", el);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }

    protected void type(By locator, String text) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
    }

    /**
     * Reads text using longWait — use for elements that depend on an async API
     * response (e.g. data rows, stat card values) which may take >15s to render.
     */
    protected String getTextSlow(By locator) {
        return longWait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
    }

    protected boolean isDisplayed(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Waits for a skeleton/loading indicator to disappear before proceeding.
     * The app shows '…' text while loading — we wait for it to be gone from
     * the target element before reading its value.
     *
     * Pass the locator of the element that will eventually contain real data.
     * Waits up to 30s for the element text to stop being "…" or empty.
     */
    protected String waitForRealText(By locator) {
        return longWait.until(driver -> {
            try {
                WebElement el = driver.findElement(locator);
                String text = el.getText().trim();
                // "…" is the loading skeleton text used throughout the app
                if (text.isEmpty() || text.equals("…")) return null;
                return text;
            } catch (Exception e) {
                return null;
            }
        });
    }

    /**
     * Waits for the loading skeleton rows to disappear from a table tbody.
     * The app renders SkeletonRows while API data is in flight — we must wait
     * for real <td> content before reading row data.
     *
     * Waits until at least one real (non-skeleton) <td> is visible in the tbody.
     */
    protected void waitForTableData(By tbodyLocator) {
        longWait.until(driver -> {
            try {
                WebElement tbody = driver.findElement(tbodyLocator);
                // Skeleton rows have <td> with a child .skeleton div — real rows don't
                // We check that at least one td has non-empty text
                return tbody.findElements(By.xpath(".//td[string-length(normalize-space())>0]"))
                            .stream()
                            .anyMatch(WebElement::isDisplayed);
            } catch (Exception e) {
                return false;
            }
        });
    }
}