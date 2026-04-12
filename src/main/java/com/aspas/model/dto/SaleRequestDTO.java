package com.aspas.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

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