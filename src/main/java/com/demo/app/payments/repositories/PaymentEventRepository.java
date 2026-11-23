package com.demo.app.payments.repositories;

import com.demo.app.payments.model.entities.PaymentEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentEventRepository extends JpaRepository<PaymentEventEntity, UUID>
{
    Optional<PaymentEventEntity> findByEventId(String eventId);
}
