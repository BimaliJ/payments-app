package com.demo.app.payments.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DestinationAccountRequest(
        @NotBlank String name,
        @NotBlank
        @Pattern(regexp="^[0-9]{10,20}$", message="Destination account must contain 10 to 20 digits")
        String accountNumber
) {
}
