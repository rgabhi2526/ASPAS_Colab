package com.aspas.qa.tests;

import com.aspas.qa.pages.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AspasE2ETests {

    private WebDriver driver;
    private DashboardPage dashboardPage;
    private SalesPage salesPage;
    private InventoryPage inventoryPage;
    private OrdersPage ordersPage;
    private ReportsPage reportsPage;

    @BeforeMethod
    public void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        driver = new ChromeDriver(options);

        driver.get("http://127.0.0.1:5500/ASPAS%20Design%20System/ui_kits/dashboard/index.html");

        dashboardPage = new DashboardPage(driver);
        salesPage     = new SalesPage(driver);
        inventoryPage = new InventoryPage(driver);
        ordersPage    = new OrdersPage(driver);
        reportsPage   = new ReportsPage(driver);
    }

    @Test(description = "Verify Today's Revenue updates correctly after a sale")
    public void testSalesToDashboardIntegration() {
        final String PART_NUMBER     = "SP-BRK-001";
        final double PART_UNIT_PRICE = 450.00;
        final int    SALE_QTY        = 2;

        double transactionTotal = PART_UNIT_PRICE * SALE_QTY; // 900.0

        dashboardPage.navigateToDashboard();
        double initialRevenue = dashboardPage.getTodaysRevenue();

        salesPage.navigateToSales();
        salesPage.processTransaction(PART_NUMBER, SALE_QTY);

        dashboardPage.navigateToDashboard();
        double newRevenue = dashboardPage.getTodaysRevenue();

        Assert.assertEquals(newRevenue, initialRevenue + transactionTotal, 0.01,
            "Revenue did not increase by the exact transaction amount.");
    }

    @Test(description = "Verify JIT Trigger drops part to low stock and generates order")
    public void testJitTriggerAndOrderGeneration() {
        final String PART_NUMBER = "SP-ENG-001";               // update if different in your DB
        final String PART_NAME   = "Spark Plug - Iridium";     // confirmed exact DB name

        // 1. Read current inventory state
        inventoryPage.navigateToInventory();
        int currentQty = inventoryPage.getPartQuantity(PART_NAME);
        int threshold  = inventoryPage.getPartThreshold(PART_NAME);

        int saleQty = (currentQty - threshold) + 1;
        Assert.assertTrue(saleQty > 0,
            "Part is already at or below threshold — reset DB before running this test.");

        // 2. Sell enough units to push stock below JIT threshold
        salesPage.navigateToSales();
        salesPage.processTransaction(PART_NUMBER, saleQty);

        // 3. Go to Reports and recalculate JIT thresholds so the system
        //    recognises the part as below threshold before order generation
        reportsPage.navigateToReports();
        reportsPage.recalculateJIT();

        // 4. Go to Orders and trigger bulk restocking order generation
        ordersPage.navigateToOrders();
        ordersPage.generateRestockingOrder(PART_NAME);

        // 5. Verify an order was created containing this part
        Assert.assertTrue(ordersPage.isOrderPresent(PART_NAME),
            "Restocking order containing '" + PART_NAME + "' was not found.");

        // 6. Verify the Dashboard now shows the part in Low Stock Alerts
        dashboardPage.navigateToDashboard();
        Assert.assertTrue(dashboardPage.isPartInLowStockAlerts(PART_NAME),
            "Part '" + PART_NAME + "' should appear in Low Stock Alerts on the dashboard.");
    }

    @Test(description = "Verify adding and deleting a spare part in inventory")
    public void testInventoryManagementAddDelete() {
        final String NEW_PART_NAME = "QA Test Alternator";

        inventoryPage.navigateToInventory();

        inventoryPage.addNewPart(NEW_PART_NAME, 50, 10, 120.00);
        Assert.assertTrue(inventoryPage.isPartPresent(NEW_PART_NAME),
            "Newly added part should be visible in the inventory table.");

        inventoryPage.deletePart(NEW_PART_NAME);
        Assert.assertFalse(inventoryPage.isPartPresent(NEW_PART_NAME),
            "Deleted part should no longer be visible in the inventory table.");
    }

    @AfterMethod
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}