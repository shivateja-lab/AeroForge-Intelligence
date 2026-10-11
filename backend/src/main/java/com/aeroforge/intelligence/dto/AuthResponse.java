package com.aeroforge.intelligence.dto;

import com.aeroforge.intelligence.entity.UserRole;

public record AuthResponse(
    String token,
    String tokenType,
    long expiresIn,
    Long userId,
    String fullName,
    String email,
    UserRole role,
    String department
) {}
