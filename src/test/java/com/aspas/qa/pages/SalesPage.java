package com.aspas.qa.pages;

import com.aspas.qa.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SalesPage extends BasePage {

    // ── Navigation ──────────────────────────────────────────────────────────
    private final By navSales = By.xpath("//nav//button[normalize-space()='Sales']");

    // ── Record Sale flow ─────────────────────────────────────────────────────
    private final By recordSaleBtn = By.xpath("//button[normalize-space()='Record Sale']");
    private final By modalHeading  = By.xpath("//h2[normalize-space()='Record Sale']");

    private final By partNumberInput = By.xpath(
        "//h2[normalize-space()='Record Sale']/ancestor::div[2]//label[normalize-space()='Part Number']/following-sibling::input");
    private final By quantityInput = By.xpath(
        "//h2[normalize-space()='Record Sale']/ancestor::div[2]//label[normalize-space()='Quantity Sold']/following-sibling::input");
    private final By submitSaleBtn = By.xpath(
        "//h2[normalize-space()='Record Sale']/ancestor::div[2]//button[normalize-space()='Record Sale']");

    // Toast: the error log confirmed the browser renders the color as rgb(240, 253, 244),
    // NOT the hex #F0FDF4 we had before. Match on the unique z-index + bottom position
    // that are stable properties of the Toast component.
    private final By successToast = By.xpath(
        "//div[contains(@style,'z-index: 200') or contains(@style,'zIndex: 200')]"
        + "[contains(@style,'bottom: 24px') or contains(@style,'bottom:24px')]"
        + "[contains(@style,'rgb(240, 253, 244)')]");

    public SalesPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToSales() {
        click(navSales);
    }

    /**
     * Opens "Record Sale" modal, fills Part Number + Quantity, submits,
     * then waits for the green success toast to confirm backend processed the sale.
     *
     * Pass the part's PART NUMBER (e.g. "SP-BRK-001"), not its display name.
     */
    public void processTransaction(String partNumber, int quantity) {
        click(recordSaleBtn);
        wait.until(ExpectedConditions.visibilityOfElementLocated(modalHeading));

        type(partNumberInput, partNumber);

        driver.findElement(quantityInput).clear();
        type(quantityInput, String.valueOf(quantity));

        click(submitSaleBtn);

        // Wait for the green success toast — confirms the POST /sales call succeeded.
        wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));

        // Also wait for it to disappear (auto-dismisses after 3s in the app) so it
        // doesn't block any subsequent clicks in the calling test.
        wait.until(ExpectedConditions.invisibilityOfElementLocated(successToast));
    }
}