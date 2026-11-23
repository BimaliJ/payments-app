package com.demo.app.payments.service;

import com.demo.app.payments.exception.InvalidCurrencyException;
import com.demo.app.payments.exception.PaymentNotFoundException;
import com.demo.app.payments.mapper.PaymentMapper;
import com.demo.app.payments.model.enums.ConsumerType;
import com.demo.app.payments.model.PaymentRequest;
import com.demo.app.payments.model.PaymentResponse;
import com.demo.app.payments.model.entities.EventDeliveryEntity;
import com.demo.app.payments.model.entities.PaymentEntity;
import com.demo.app.payments.model.entities.PaymentEventEntity;
import com.demo.app.payments.repositories.EventDeliveryRepository;
import com.demo.app.payments.repositories.PaymentEventRepository;
import com.demo.app.payments.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final EventDeliveryRepository eventDeliveryRepository;
    private final PaymentMapper paymentMapper;

    private final Set<String> supportedCurrencies;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request)
    {
        validateCurrency(request.currency());

        //create payment entity
        PaymentEntity paymentEntity = paymentMapper.toEntity(request);

        //save payment
        PaymentEntity savedEntity= paymentRepository.save(paymentEntity);

        PaymentEventEntity paymentEventEntity=paymentEventRepository.save(PaymentEventEntity.submitted(paymentEntity.getId()));
        for(ConsumerType consumer: ConsumerType.values())
        {
            eventDeliveryRepository.save(EventDeliveryEntity.pending(paymentEventEntity.getId(),consumer));
        }

        return paymentMapper.toResponse(savedEntity);
    }

    @Transactional(readOnly = true)
    public PaymentResponse findById(UUID id)
    {
        PaymentEntity payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new PaymentNotFoundException("Payment not found"));

        return paymentMapper.toResponse(payment);
    }

    private void validateCurrency(String currency) {

        String normalisedCurrency =
                currency.trim().toUpperCase(Locale.ROOT);

        if (!supportedCurrencies.contains(normalisedCurrency)) {
            throw new InvalidCurrencyException(
                    "Unsupported currency: " + currency);
        }
    }
}