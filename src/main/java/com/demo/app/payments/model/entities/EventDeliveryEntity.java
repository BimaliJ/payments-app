package com.demo.app.payments.model.entities;

import com.demo.app.payments.model.enums.ConsumerType;
import com.demo.app.payments.model.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="event_deliveries",
        uniqueConstraints=@UniqueConstraint(name="uk_event_consumer",columnNames={"event_id","consumer"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDeliveryEntity
{
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    @Column
    private UUID id;

    @Column(name="event_id")
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column
    private ConsumerType consumer;

    @Enumerated(EnumType.STRING)
    @Column
    private DeliveryStatus status;

    @Column
    private int retryCount;

    @Column
    private Instant nextRetryAt;

    @Column(length=500) private String lastError;
    @Column(nullable=false,updatable=false) private Instant createdAt;
    public static EventDeliveryEntity pending(UUID eventId, ConsumerType consumer) {
        return EventDeliveryEntity.builder().eventId(eventId).consumer(consumer).status(DeliveryStatus.PENDING)
                .retryCount(0).createdAt(Instant.now()).build();
    }
}
