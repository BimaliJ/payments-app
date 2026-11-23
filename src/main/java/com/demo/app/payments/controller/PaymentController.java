package com.demo.app.payments.controller;

import com.demo.app.payments.model.PaymentRequest;
import com.demo.app.payments.model.PaymentResponse;
import com.demo.app.payments.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController
{
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<?> createPayment(
            @Valid @RequestBody PaymentRequest request) {

            PaymentResponse response = paymentService.createPayment(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(response);

    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable UUID id)
    {
            return ResponseEntity.ok(paymentService.findById(id));
    }
}
