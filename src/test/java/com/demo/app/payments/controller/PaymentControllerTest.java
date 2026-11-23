package com.demo.app.payments.controller;

import com.demo.app.payments.exception.GlobalExceptionHandler;
import com.demo.app.payments.exception.PaymentNotFoundException;
import com.demo.app.payments.model.PaymentResponse;
import com.demo.app.payments.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PaymentControllerTest
{
    private PaymentService service;
    private MockMvc mvc;
    private final UUID id = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = mock(PaymentService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new PaymentController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator).build();
    }

    private String validRequest() {
        return """
                {"sourceAccount":"1234567890", "destinationAccount":{"name":"Test Payee","accountNumber":"1234567890"},
                 "amount":25.50,"currency":"NZD","reference":"Invoice 123","requestedExecutionDate":"2099-01-01"}
                """;
    }

    @Test
    void createPaymentReturns201() throws Exception {
        when(service.createPayment(any())).thenReturn(new PaymentResponse(id, null, null, null, null, null, null, "SUBMITTED", null, null, null, null));
        mvc.perform(post("/payments").contentType(MediaType.APPLICATION_JSON).content(validRequest()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(id.toString()));
        verify(service).createPayment(any());
    }

    @Test
    void invalidPaymentReturns400() throws Exception {
        mvc.perform(post("/payments").contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest().replace("25.50", "-1.00")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        verifyNoInteractions(service);
    }

    @Test
    void getPaymentReturns200() throws Exception {
        when(service.findById(id)).thenReturn(new PaymentResponse(id, null, null, null, null, null, null, "SUBMITTED", null, null, null, null));
        mvc.perform(get("/payments/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void missingPaymentReturns404() throws Exception {
        when(service.findById(id)).thenThrow(new PaymentNotFoundException("Payment not found"));
        mvc.perform(get("/payments/{id}", id)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PAYMENT_NOT_FOUND"));
    }
}
