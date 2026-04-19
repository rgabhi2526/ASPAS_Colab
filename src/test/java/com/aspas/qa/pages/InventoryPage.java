package com.aspas.qa.pages;

import com.aspas.qa.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class InventoryPage extends BasePage {

    // ── Navigation ──────────────────────────────────────────────────────────
    private final By navInventory  = By.xpath("//nav//button[normalize-space()='Inventory']");

    // ── Inventory table ──────────────────────────────────────────────────────
    // We wait for this tbody to have real data before reading any rows
    private final By inventoryTbody = By.xpath("//table/tbody");

    // ── Add Part flow ───────────────────────────────────────────────────────
    private final By addPartBtn    = By.xpath("//button[normalize-space()='Add Part']");
    private final By modalHeading  = By.xpath("//h2[normalize-space()='Add Spare Part']");

    private final By partNumberInput = By.xpath(
        "//h2[normalize-space()='Add Spare Part']/ancestor::div[2]//label[normalize-space()='Part Number']/following-sibling::input");
    private final By partNameInput = By.xpath(
        "//h2[normalize-space()='Add Spare Part']/ancestor::div[2]//label[normalize-space()='Part Name']/following-sibling::input");
    private final By unitPriceInput = By.xpath(
        "//h2[normalize-space()='Add Spare Part']/ancestor::div[2]//label[contains(normalize-space(),'Unit Price')]/following-sibling::input");
    private final By qtyInput = By.xpath(
        "//h2[normalize-space()='Add Spare Part']/ancestor::div[2]//label[normalize-space()='Current Quantity']/following-sibling::input");
    private final By thresholdInput = By.xpath(
        "//h2[normalize-space()='Add Spare Part']/ancestor::div[2]//label[normalize-space()='Threshold Value']/following-sibling::input");
    private final By submitPartBtn = By.xpath(
        "//h2[normalize-space()='Add Spare Part']/ancestor::div[2]//button[normalize-space()='Add Part']");

    // ── Delete flow ──────────────────────────────────────────────────────────
    private final By confirmDeleteBtn = By.xpath(
        "//h2[normalize-space()='Delete Part']/ancestor::div[2]//button[normalize-space()='Delete']");

    // ── Toast ────────────────────────────────────────────────────────────────
    // Unique to Toast component: z-index 200 + bottom 24px
    private final By anyToast = By.xpath(
        "//div[contains(@style,'z-index: 200')][contains(@style,'bottom: 24px')]");

    public InventoryPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToInventory() {
        click(navInventory);
        // Wait for the inventory table to finish loading API data before any reads
        waitForTableData(inventoryTbody);
    }

    public void addNewPart(String name, int qty, int threshold, double price) {
        click(addPartBtn);
        wait.until(ExpectedConditions.visibilityOfElementLocated(modalHeading));

        String sanitized = name.toUpperCase().replaceAll("[^A-Z0-9 ]", "").trim();
        String partNumber = sanitized.replaceAll("\\s+", "-");
        partNumber = partNumber.substring(0, Math.min(12, partNumber.length()));

        type(partNumberInput, partNumber);
        type(partNameInput, name);
        type(unitPriceInput, String.valueOf(price));
        type(qtyInput, String.valueOf(qty));
        type(thresholdInput, String.valueOf(threshold));
        click(submitPartBtn);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(modalHeading));

        // Wait for success toast to appear then fully disappear (auto-dismisses in 3s)
        // before any subsequent actions — prevents it intercepting the delete button
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(anyToast));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(anyToast));
        } catch (org.openqa.selenium.TimeoutException ignored) {
            // Toast may have flashed too quickly — safe to continue
        }
    }

    public boolean isPartPresent(String partName) {
        return isDisplayed(By.xpath(
            String.format("//tbody//td[contains(normalize-space(),'%s')]", partName)));
    }

    public void deletePart(String partName) {
        // Guarantee no toast is covering any part of the table
        wait.until(ExpectedConditions.invisibilityOfElementLocated(anyToast));

        By deleteIcon = By.xpath(
            String.format("//tr[td[contains(normalize-space(),'%s')]]//button", partName));

        // JS click — avoids any residual overlay intercept that a normal click triggers
        jsClick(deleteIcon);

        // Wait for confirm dialog then click Delete
        click(confirmDeleteBtn);

        // Wait for the row to leave the DOM — use longWait because the DELETE
        // API call + React re-render can take longer than 15s on slow backends
        longWait.until(ExpectedConditions.invisibilityOfElementLocated(
            By.xpath(String.format("//tbody//td[contains(normalize-space(),'%s')]", partName))));
    }

    /**
     * Table columns: Part No. | Part Name | Size | Qty | Threshold | Price | Location | Status | (delete)
     * Qty = td[4], Threshold = td[5]
     */
    public int getPartQuantity(String partName) {
        // Table data is already loaded by navigateToInventory(), but double-check the row
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
            String.format("//tbody//td[contains(normalize-space(),'%s')]", partName))));
        String qty = getText(By.xpath(
            String.format("//tr[td[contains(normalize-space(),'%s')]]/td[4]", partName)));
        return Integer.parseInt(qty.trim());
    }

    public int getPartThreshold(String partName) {
        String threshold = getText(By.xpath(
            String.format("//tr[td[contains(normalize-space(),'%s')]]/td[5]", partName)));
        return Integer.parseInt(threshold.trim());
    }
}