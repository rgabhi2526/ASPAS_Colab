package com.aspas.util;

import com.aspas.model.entity.OrderItem;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ================================================================
 * OrderFormatter — Print-Ready Text Output Utility
 * ================================================================
 *
 * UML Traceability:
 *   - Sequence Diagram: Message #23 "OL → OL : formatOutput()"
 *   - Interface: Supports Printable.formatOutput()
 *   - UC-03: Generate Daily Orders → printed output
 *
 * Generates the text-based order list output.
 * Format: Part# | Required Qty | Vendor Address
 * (As specified in the project requirements)
 *
 * ================================================================
 */
public class OrderFormatter {

    private static final String DOUBLE_LINE = "══════════════════════════════════════════════════════════════════";
    private static final String SINGLE_LINE = "──────────────────────────────────────────────────────────────────";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    /**
     * Format a complete order list as print-ready text.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #23
     *   Output matches project requirement:
     *     "printed order list containing part number,
     *      required quantity, and vendor's address"
     *
     * @param orderId order identifier
     * @param orderDate date of the order
     * @param items list of order items
     * @param generatedAt timestamp of generation
     * @return formatted text string
     */
    public static String formatOrderList(
            Long orderId,
            LocalDate orderDate,
            List<OrderItem> items,
            LocalDateTime generatedAt
    ) {
        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("\n").append(DOUBLE_LINE).append("\n");
        sb.append("              ASPAS — END OF DAY ORDER LIST\n");
        sb.append("     Automobile Spare Parts Shop Automation System\n");
        sb.append(DOUBLE_LINE).append("\n");
        sb.append(String.format("  Order ID   : %d\n", orderId != null ? orderId : 0));
        sb.append(String.format("  Order Date : %s\n", orderDate.format(DATE_FORMAT)));
        sb.append(String.format("  Generated  : %s\n", generatedAt.format(DATETIME_FORMAT)));
        sb.append(SINGLE_LINE).append("\n");

        if (items == null || items.isEmpty()) {
            sb.append("\n  ✓ All parts are above JIT threshold.\n");
            sb.append("  ✓ No orders required today.\n\n");
        } else {
            // Column Headers
            sb.append(String.format(
                "  %-5s │ %-12s │ %-22s │ %-5s │ %-20s │ %s\n",
                "LINE", "PART#", "PART NAME", "QTY", "VENDOR", "ADDRESS"
            ));
            sb.append(SINGLE_LINE).append("\n");

            // Order Items
            int lineNo = 1;
            for (OrderItem item : items) {
                sb.append(String.format(
                    "  %-5d │ %-12s │ %-22s │ %-5d │ %-20s │ %s\n",
                    lineNo++,
                    truncate(item.getPartNumber(), 12),
                    truncate(item.getPartName() != null ? item.getPartName() : "N/A", 22),
                    item.getRequiredQuantity(),
                    truncate(item.getVendorName() != null ? item.getVendorName() : "N/A", 20),
                    item.getVendorAddress()
                ));
            }

            sb.append(SINGLE_LINE).append("\n");
            sb.append(String.format("  Total Distinct Parts to Reorder: %d\n", items.size()));

            // Calculate total units
            int totalUnits = items.stream()
                .mapToInt(OrderItem::getRequiredQuantity)
                .sum();
            sb.append(String.format("  Total Units to Order           : %d\n", totalUnits));
        }

        // Footer
        sb.append(SINGLE_LINE).append("\n");
        sb.append("  JIT Policy: Stock maintained for exactly 1-week demand\n");
        sb.append("  Threshold = Total sales of last 7 days per part\n");
        sb.append(DOUBLE_LINE).append("\n");

        return sb.toString();
    }

    /**
     * Truncate a string to a maximum length.
     */
    private static String truncate(String value, int maxLength) {
        if (value == null) return "N/A";
        return value.length() > maxLength
            ? value.substring(0, maxLength - 2) + ".."
            : value;
    }
}