package com.demo.app.payments.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        String sourceAccount,
        DestinationAccountResponse destinationAccount,
        BigDecimal amount,
        String currency,
        String reference,
        LocalDate requestedExecutionDate,
        String status,
        String fraudDecision,
        BigDecimal fraudRiskScore,
        List<String> fraudReasons,
        Instant createdAt
) {
}
