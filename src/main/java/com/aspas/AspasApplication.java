package com.aspas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ================================================================
 * ASPAS - Automobile Spare Parts Shop Automation System
 * ================================================================
 * 
 * Main entry point for the Spring Boot backend application.
 * 
 * This system implements:
 *   - JIT (Just-In-Time) inventory management
 *   - Automated end-of-day order generation
 *   - Dynamic threshold calculation based on 7-day sales average
 *   - Daily revenue logging and monthly graph data generation
 * 
 * Architecture:
 *   - MySQL  : Inventory, Vendors, Orders (relational/transactional)
 *   - MongoDB: Sales Transactions (high-write analytics)
 * 
 * UML Traceability:
 *   - DFD Processes P1-P4 → Service Layer
 *   - Class Diagram       → model/entity + model/document packages
 *   - Sequence Diagram    → Service method call chains
 *   - Use Case Diagram    → REST Controller endpoints
 *   - Activity Diagram    → JITService loop logic
 *   - System Clock Actor  → EndOfDayScheduler (@Scheduled)
 * 
 * @author ASPAS Team
 * @version 1.0.0
 * ================================================================
 */
@SpringBootApplication
@EnableScheduling      // Enables System Clock actor (@Scheduled cron jobs)
@EnableJpaAuditing     // Enables automatic timestamp management
public class AspasApplication {

    public static void main(String[] args) {
        SpringApplication.run(AspasApplication.class, args);

        System.out.println("============================================");
        System.out.println("  ASPAS Backend Started Successfully");
        System.out.println("  Swagger UI : http://localhost:8080/swagger-ui.html");
        System.out.println("  API Docs   : http://localhost:8080/api-docs");
        System.out.println("============================================");
    }
}