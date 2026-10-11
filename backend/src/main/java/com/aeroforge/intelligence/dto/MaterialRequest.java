package com.aeroforge.intelligence.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;

public record MaterialRequest(
    @NotBlank String materialCode,
    @NotBlank String materialName,
    @NotBlank String alloyGrade,
    String supplierName,
    @NotBlank String batchNumber,
    BigDecimal thicknessMm,
    BigDecimal densityGCm3,
    String status
) {}
