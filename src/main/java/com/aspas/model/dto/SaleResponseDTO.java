package com.aspas.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDateTime;

/**
 * ================================================================
 * SaleResponseDTO
 * ================================================================
 * 
 * Response from sale processing.
 * Returned via REST API: 201 Created
 * 
 * UML Traceability:
 *   - Sequence Diagram: Message #8 "SC → Owner : success"
 * 
 * ================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleResponseDTO {

    private String transactionId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime transactionDate;
    
    private String partNumber;
    private String partName;
    private Integer quantitySold;
    private Double unitPrice;
    private Double revenueAmount;
    private String message;
}