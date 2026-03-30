package com.aspas.exception;

/**
 * ================================================================
 * OrderNotFoundException
 * ================================================================
 *
 * Thrown when an order list lookup fails.
 *
 * UML Traceability:
 *   - UC-03: Generate Daily Orders — retrieval of past orders
 *
 * ================================================================
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long orderId) {
        super("Order not found with ID: " + orderId);
    }

    public OrderNotFoundException(String message) {
        super(message);
    }
}