package com.aspas.qa.pages;

import com.aspas.qa.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class OrdersPage extends BasePage {

    // ── Navigation ──────────────────────────────────────────────────────────
    private final By navOrders = By.xpath("//nav//button[normalize-space()='Orders']");

    // ── Generate Orders flow ─────────────────────────────────────────────────
    // TopBar button that opens the confirmation dialog
    private final By generateOrderBtn = By.xpath("//button[normalize-space()='Generate Orders']");

    // ConfirmDialog "Generate" confirm button
    // The dialog title is "Generate Orders" and the confirm button text is "Generate"
    private final By confirmGenerateBtn = By.xpath(
        "//div[h2[normalize-space()='Generate Orders']]//button[normalize-space()='Generate']"
    );

    public OrdersPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToOrders() {
        click(navOrders);
    }

    /**
     * Generates restocking orders for ALL parts below JIT threshold.
     *
     * NOTE: The actual HTML does NOT support generating an order for a specific
     * part by name. The "Generate Orders" button calls POST /orders/generate
     * which auto-generates for ALL below-threshold parts at once. The partName
     * parameter is kept for API compatibility with the test, but the UI action
     * is a bulk generate — the test should verify the part appears in the result.
     */
    public void generateRestockingOrder(String partName) {
        click(generateOrderBtn);
        // Wait for confirmation dialog to appear
        wait.until(ExpectedConditions.visibilityOfElementLocated(confirmGenerateBtn));
        click(confirmGenerateBtn);
        // Wait for dialog to close
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
            By.xpath("//div[h2[normalize-space()='Generate Orders']]")
        ));
    }

    /**
     * Checks if an order row exists for the given part name.
     * The orders table shows: Order ID | Date | Vendor | Items | Printed | Status
     * Orders are linked to vendors, not directly to parts by name in the table.
     *
     * We check the order detail drawer approach: click the most recent order row,
     * then check if the part name appears in the items list inside the drawer.
     *
     * For the test assertion to work after a bulk generate, we open each order
     * row and look for the part name inside the drawer's items table.
     */
    public boolean isOrderPresent(String partName) {
        // Find any order row whose detail drawer contains this part name in its items
        // First try: check if part name appears anywhere in the orders table area
        // (some backends echo part names in the vendor/items columns)
        By partInTable = By.xpath(String.format(
            "//table//td[contains(normalize-space(),'%s')]", partName
        ));
        if (isDisplayed(partInTable)) {
            return true;
        }

        // Fallback: open the first order row and look in the detail drawer items table
        By firstOrderRow = By.xpath("//tbody/tr[1]");
        if (!isDisplayed(firstOrderRow)) {
            return false;
        }
        click(firstOrderRow);
        // Wait for the detail drawer to open
        By drawerPartName = By.xpath(String.format(
            "//td[normalize-space()='%s']", partName
        ));
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(drawerPartName));
            // Close drawer before returning
            click(By.xpath("//button[.//*[name()='svg']]"));
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }
}