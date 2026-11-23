package com.demo.app.payments.service;

import com.demo.app.payments.exception.InvalidCurrencyException;
import com.demo.app.payments.exception.PaymentNotFoundException;
import com.demo.app.payments.mapper.PaymentMapper;
import com.demo.app.payments.model.DestinationAccountRequest;
import com.demo.app.payments.model.PaymentRequest;
import com.demo.app.payments.model.PaymentResponse;
import com.demo.app.payments.model.entities.EventDeliveryEntity;
import com.demo.app.payments.model.entities.PaymentEntity;
import com.demo.app.payments.model.entities.PaymentEventEntity;
import com.demo.app.payments.model.enums.ConsumerType;
import com.demo.app.payments.model.enums.DeliveryStatus;
import com.demo.app.payments.model.enums.PaymentEventType;
import com.demo.app.payments.repositories.EventDeliveryRepository;
import com.demo.app.payments.repositories.PaymentEventRepository;
import com.demo.app.payments.repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class PaymentServiceTest
{
    private PaymentRepository payments;
    private PaymentEventRepository events;
    private EventDeliveryRepository deliveries;
    private PaymentMapper mapper;
    private PaymentService service;

    @BeforeEach
    void setUp() {
        payments = mock(PaymentRepository.class);
        events = mock(PaymentEventRepository.class);
        deliveries = mock(EventDeliveryRepository.class);
        mapper = mock(PaymentMapper.class);
        service = new PaymentService(payments, events, deliveries, mapper, Set.of("NZD", "AUD"));
    }

    private PaymentRequest request(String currency) {
        return new PaymentRequest("1234567890", new DestinationAccountRequest("Payee", "1234567890"),
                new BigDecimal("25.50"), currency, "Invoice", LocalDate.now());
    }

    @Test
    void createPaymentSavesPaymentEventAndThreeDeliveries() {
        UUID paymentId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        PaymentEntity entity = new PaymentEntity();
        entity.setId(paymentId);
        PaymentEventEntity savedEvent = new PaymentEventEntity();
        savedEvent.setId(eventId);
        PaymentResponse response = new PaymentResponse(paymentId, null, null, null, "NZD", null, null, "SUBMITTED", null, null, null, null);
        when(mapper.toEntity(any())).thenReturn(entity);
        when(payments.save(entity)).thenReturn(entity);
        when(events.save(any(PaymentEventEntity.class))).thenReturn(savedEvent);
        when(mapper.toResponse(entity)).thenReturn(response);

        assertSame(response, service.createPayment(request("NZD")));
        verify(payments).save(entity);
        ArgumentCaptor<PaymentEventEntity> eventCaptor = ArgumentCaptor.forClass(PaymentEventEntity.class);
        verify(events).save(eventCaptor.capture());
        assertEquals(paymentId, eventCaptor.getValue().getPaymentId());
        assertEquals(PaymentEventType.PAYMENT_SUBMITTED, eventCaptor.getValue().getEventType());
        ArgumentCaptor<EventDeliveryEntity> deliveryCaptor = ArgumentCaptor.forClass(EventDeliveryEntity.class);
        verify(deliveries, times(ConsumerType.values().length)).save(deliveryCaptor.capture());
        assertEquals(EnumSet.allOf(ConsumerType.class),
                EnumSet.copyOf(deliveryCaptor.getAllValues().stream().map(EventDeliveryEntity::getConsumer).toList()));
        assertTrue(deliveryCaptor.getAllValues().stream().allMatch(d ->
                eventId.equals(d.getEventId()) && d.getStatus() == DeliveryStatus.PENDING));
    }

    @Test
    void unsupportedCurrencyDoesNotSaveAnything() {
        assertThrows(InvalidCurrencyException.class, () -> service.createPayment(request("USD")));
        verifyNoInteractions(payments, events, deliveries, mapper);
    }

    @Test
    void findExistingPaymentReturnsResponse() {
        UUID id = UUID.randomUUID();
        PaymentEntity entity = new PaymentEntity();
        PaymentResponse response = new PaymentResponse(id, null, null, null, null, null, null, "SUBMITTED", null, null, null, null);
        when(payments.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);
        assertSame(response, service.findById(id));
    }

    @Test
    void findMissingPaymentThrowsNotFound() {
        UUID id = UUID.randomUUID();
        when(payments.findById(id)).thenReturn(Optional.empty());
        assertThrows(PaymentNotFoundException.class, () -> service.findById(id));
    }
}
