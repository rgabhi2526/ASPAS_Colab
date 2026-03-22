package com.aspas.model.interfaces;

/**
 * ================================================================
 * Printable Interface
 * ================================================================
 * 
 * UML Traceability:
 *   - Class Diagram: <<interface>> Printable
 *   - Realization: OrderList ◁╌╌ Printable
 *   - Realization: Report ◁╌╌ Printable
 * 
 * Contract: Any object implementing Printable must provide
 * a formatted text output suitable for printing.
 * 
 * ================================================================
 */
public interface Printable {

    /**
     * Format the object as a print-ready string.
     * @return formatted text ready for printing
     */
    String formatOutput();

    /**
     * Print the formatted output (backend: write to file/console/printer).
     */
    void print();
}