package com.aspas.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequestDTO {

    private String reportType;  

    @Min(value = 1)
    @Max(value = 12)
    private Integer month;

    @Min(value = 2000)
    @Max(value = 2100)
    private Integer year;

    private String date;  
}