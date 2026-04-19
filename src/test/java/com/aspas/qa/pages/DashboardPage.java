package com.aspas.qa.pages;

import com.aspas.qa.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DashboardPage extends BasePage {

    private final By navDashboard = By.xpath("//nav//button[normalize-space()='Dashboard']");

    // The Dashboard stat cards are rendered in a 4-column grid.
    // "Today's Revenue" is the 2nd card. Each card has:
    //   - A label div (uppercase small text)
    //   - A value div (DM Mono font, large)
    //   - A sub-label div
    // While loading, the value div shows "…". We must wait for real text.
    // We locate it by finding the card whose label contains "Revenue", then
    // grabbing its large monospace value div (font-size 26 in inline style).
    private final By todaysRevenueCard = By.xpath(
        "//div[div[div[contains(text(),\"Today's Revenue\")]]]"
    );
    private final By todaysRevenueValue = By.xpath(
        "//div[div[div[contains(text(),\"Today's Revenue\")]]]"
        + "//div[contains(@style,'26') and contains(@style,'Mono')]"
    );

    // Low Stock Alerts table: part names are in the second <td> (Part Name column)
    // within the card whose heading is "Low Stock Alerts"
    private static final String LOW_STOCK_PART_XPATH =
        "//div[div[normalize-space()='Low Stock Alerts']]//td[normalize-space()='%s']";

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToDashboard() {
        click(navDashboard);
    }

    /**
     * Returns today's revenue as a double.
     * Waits for the value to finish loading (skeleton shows "…" while in flight).
     * The fmt() helper renders values as "₹1,250" — we strip non-numeric chars.
     */
    public double getTodaysRevenue() {
        // waitForRealText keeps retrying until the element text is not "…" or empty
        String raw = waitForRealText(todaysRevenueValue);
        // Strip ₹, commas, spaces — keep digits and decimal point
        String clean = raw.replaceAll("[^\\d.]", "");
        return clean.isEmpty() ? 0.0 : Double.parseDouble(clean);
    }

    public boolean isPartInLowStockAlerts(String partName) {
        By row = By.xpath(String.format(LOW_STOCK_PART_XPATH, partName));
        return isDisplayed(row);
    }
}