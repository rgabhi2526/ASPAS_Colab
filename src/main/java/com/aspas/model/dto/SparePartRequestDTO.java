package com.aspas.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class SparePartRequestDTO {

    @NotBlank(message = "Part number cannot be blank")
    private String partNumber;

    @NotBlank(message = "Part name cannot be blank")
    private String partName;

    @Min(value = 0, message = "Current quantity cannot be negative")
    private Integer currentQuantity = 0;

    @Min(value = 0, message = "Threshold cannot be negative")
    private Integer thresholdValue = 0;

    @NotNull(message = "Unit price is required")
    private Double unitPrice;

    private String sizeCategory = "MEDIUM";

    private List<Long> vendorIds;
}
