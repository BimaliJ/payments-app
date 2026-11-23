package com.demo.app.payments.model.entities;

import com.demo.app.payments.model.enums.PaymentEventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEventEntity
{
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private PaymentEventType eventType;

    @Column
    private String status;

    @Column(name = "event_id")
    private String eventId;

    @Column
    private String payload;

    @Column
    private Instant createdAt;

    public static PaymentEventEntity submitted(UUID paymentId) {
        return PaymentEventEntity.builder()
                .paymentId(paymentId)
                .eventType(PaymentEventType.PAYMENT_SUBMITTED)
                .eventId(UUID.randomUUID().toString())
                .payload("{\"paymentId\":\"" + paymentId + "\"}")
                .status("PENDING")
                .createdAt(Instant.now())
                .build();
    }

}
