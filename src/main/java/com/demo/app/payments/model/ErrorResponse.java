package com.demo.app.payments.model;

public record ErrorResponse(
        String code,
        String message
) {
}
