package com.aspas.qa.pages;

import com.aspas.qa.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ReportsPage extends BasePage {

    // ── Navigation ──────────────────────────────────────────────────────────
    private final By navReports = By.xpath("//nav//button[normalize-space()='Reports']");

    // ── JIT section ──────────────────────────────────────────────────────────
    // The "Recalculate JIT" button is inside the JIT Threshold Status card on the Reports screen.
    // It only appears after the /jit/thresholds API response has loaded (not during skeleton).
    private final By recalculateJitBtn = By.xpath("//button[normalize-space()='Recalculate JIT']");

    // The JIT section heading — used to confirm the Reports screen has fully loaded
    private final By jitSectionHeading = By.xpath("//div[normalize-space()='JIT Threshold Status']");

    public ReportsPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToReports() {
        click(navReports);
        // Wait for the JIT section to be visible before interacting —
        // the Reports screen makes several API calls and renders async
        wait.until(ExpectedConditions.visibilityOfElementLocated(jitSectionHeading));
    }

    /**
     * Clicks "Recalculate JIT" which POSTs to /jit/calculate,
     * then waits for the button to become clickable again (API call complete).
     * This recalculates thresholds based on 7-day sales velocity so the
     * newly below-threshold part will be picked up by order generation.
     */
    public void recalculateJIT() {
        // Wait for the button to be present — it only renders after jit/thresholds loads
        wait.until(ExpectedConditions.elementToBeClickable(recalculateJitBtn)).click();

        // The button triggers an alert() on error (see HTML source).
        // On success there is no toast — the jit.refresh() just re-fetches the counts.
        // We wait briefly for the JIT section to re-render with updated counts.
        wait.until(ExpectedConditions.visibilityOfElementLocated(jitSectionHeading));
    }
}