package com.example.observability.controller;

import jakarta.validation.constraints.NotBlank;

public record LogPayload(
    @NotBlank String level,
    @NotBlank String message,
    String appVersion,
    String userId
) {}
