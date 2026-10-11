package com.aeroforge.intelligence.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WeldRequest(
    @NotNull Long materialId,
    @NotBlank String weldCode,
    String weldingProcess,
    @NotNull @Positive BigDecimal currentAmp,
    @NotNull @Positive BigDecimal voltageV,
    @NotNull @Positive BigDecimal travelSpeedMmS,
    BigDecimal heatInputKjMm,
    String shieldingGas,
    String operatorName
) {}
