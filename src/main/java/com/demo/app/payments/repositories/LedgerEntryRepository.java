package com.demo.app.payments.repositories;

import com.demo.app.payments.model.entities.LedgerEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntryEntity, UUID>
{
    boolean existsByPaymentId(UUID paymentId);
}
