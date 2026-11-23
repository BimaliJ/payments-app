package com.demo.app.payments.service;

import com.demo.app.payments.consumer.*;
import com.demo.app.payments.model.entities.*;
import com.demo.app.payments.model.enums.*;
import com.demo.app.payments.repositories.*;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final EventDeliveryRepository deliveryRepository;
    private final PaymentEventRepository eventRepository;

    private final NotificationConsumer notificationConsumer;
    private final FraudConsumer fraudConsumer;
    private final LedgerConsumer ledgerConsumer;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void dispatch() {

        var deliveries = deliveryRepository.findReady(
                List.of(DeliveryStatus.PENDING, DeliveryStatus.RETRY),
                Instant.now(),
                PageRequest.of(0, 30)
        );

        for (EventDeliveryEntity delivery : deliveries) {

            try {
                PaymentEventEntity event = eventRepository
                        .findById(delivery.getEventId())
                        .orElseThrow();

                switch (delivery.getConsumer()) {
                    case NOTIFICATION ->
                            notificationConsumer.accept(event.getPaymentId());

                    case FRAUD ->
                            fraudConsumer.accept(event.getPaymentId());

                    case LEDGER ->
                            ledgerConsumer.accept(event.getPaymentId());
                }

                delivery.setStatus(DeliveryStatus.SUCCESS);
                delivery.setLastError(null);
                delivery.setNextRetryAt(null);

            } catch (Exception e) {

                delivery.setStatus(DeliveryStatus.RETRY);
                delivery.setRetryCount(delivery.getRetryCount() + 1);

                delivery.setNextRetryAt(
                        Instant.now().plusSeconds(10)
                );

                delivery.setLastError(
                        e.toString().substring(
                                0, Math.min(500, e.toString().length())
                        )
                );
            }

            deliveryRepository.save(delivery);
        }
    }
}
