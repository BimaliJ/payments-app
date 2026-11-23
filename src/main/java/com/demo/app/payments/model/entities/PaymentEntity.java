package com.demo.app.payments.model.entities;

import com.demo.app.payments.model.enums.FraudDecision;
import com.demo.app.payments.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity
{
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(nullable=false)
    private String sourceAccount;

    @Column(nullable=false)
    private String destinationAccountName;

    @Column(nullable=false)
    private String destinationAccountNumber;

    @Column(nullable=false)
    private BigDecimal amount;

    @Column(nullable=false)
    private String currency;

    @Column(nullable=false)
    private String reference;

    @Column(nullable=false)
    private LocalDate requestedExecutionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column
    private FraudDecision fraudDecision;

    @Column(precision=6, scale=2)
    private BigDecimal fraudRiskScore;

    @Column(length=1000)
    private String fraudReasons;

    @Column(nullable=false)
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = Instant.now();

        if (status == null) {
            status = PaymentStatus.SUBMITTED;
        }

        if (fraudDecision == null) {
            fraudDecision = FraudDecision.PENDING;
        }
    }
}
