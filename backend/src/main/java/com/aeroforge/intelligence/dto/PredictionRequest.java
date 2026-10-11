package com.aeroforge.intelligence.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PredictionRequest(
    @NotNull Long materialId,
    Long weldRecordId,
    @Min(0) @Max(20) int repairCount,
    Double hazWidthMm
) {}
