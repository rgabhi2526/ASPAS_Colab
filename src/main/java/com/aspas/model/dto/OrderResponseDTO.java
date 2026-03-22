package com.aspas.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ================================================================
 * OrderResponseDTO
 * ================================================================
 * 
 * Response containing generated order list details.
 * 
 * UML Traceability:
 *   - Sequence Diagram: Message #23 "SC → Owner : orders ready"
 *   - Use Case: UC-03 Generate Daily Orders
 * 
 * ================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {

    private Long orderId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate orderDate;

    private Integer totalItems;
    private Boolean isPrinted;
    private String printText;  // Formatted order output

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private List<OrderItemDTO> items;

    /**
     * Inner class for order items.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemDTO {
        private Long itemId;
        private String partNumber;
        private String partName;
        private Integer requiredQuantity;
        private String vendorName;
        private String vendorAddress;
    }
}