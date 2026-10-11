package com.aeroforge.intelligence.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MechanicalTestRequest(
    @NotNull Long materialId,
    Long weldRecordId,
    @NotBlank String testType,
    @NotNull @Positive BigDecimal tensileStrengthMpa,
    @NotNull @Positive BigDecimal yieldStrengthMpa,
    @NotNull @Positive BigDecimal elongationPercent,
    BigDecimal hardnessHv,
    BigDecimal fractureToughness,
    BigDecimal testTemperatureC
) {}
