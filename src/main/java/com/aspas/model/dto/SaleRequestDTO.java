package com.aspas.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * ================================================================
 * SaleRequestDTO
 * ================================================================
 * 
 * Data Transfer Object for incoming sale requests.
 * Received via REST API: POST /api/sales
 * 
 * UML Traceability:
 *   - Sequence Diagram: Message #1 "Owner → SC.processSale()"
 *   - Use Case: UC-01 Process Sale & Update Inventory
 * 
 * ================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleRequestDTO {

    @NotBlank(message = "Part number is required")
    private String partNumber;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}