package com.demo.app.payments.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentRequest(
        @NotBlank(message="Source account is required")
        @Pattern(regexp="^[0-9]{10,20}$", message="Source account must contain 10 to 20 digits")
        String sourceAccount,
        @NotNull
        @Valid
        DestinationAccountRequest destinationAccount,
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.00", inclusive = false,
                message = "Amount must be greater than zero")
        @Digits(integer = 15, fraction = 2,
                message = "Amount must have up to 2 decimal places")
        BigDecimal amount,
        @NotBlank @Pattern(regexp="^[A-Z]{3}$", message="Currency must be a 3-letter uppercase code")
        String currency,
        @NotBlank @Size(max=140)
        String reference,
        @NotNull @FutureOrPresent(message="Execution date cannot be in the past")
        LocalDate requestedExecutionDate
) {
}
