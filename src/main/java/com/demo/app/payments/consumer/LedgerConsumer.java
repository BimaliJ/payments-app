package com.demo.app.payments.consumer;

import com.demo.app.payments.model.entities.LedgerEntryEntity;
import com.demo.app.payments.repositories.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LedgerConsumer
{
    private final LedgerEntryRepository ledger;

    public void accept(UUID paymentId) {
        if (!ledger.existsByPaymentId(paymentId)) ledger.saveAndFlush(new LedgerEntryEntity(paymentId));
    }
}
