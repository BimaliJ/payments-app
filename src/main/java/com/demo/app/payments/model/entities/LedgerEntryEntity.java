package com.demo.app.payments.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="ledger_entries",
        uniqueConstraints=@UniqueConstraint(name="uk_ledger_payment",columnNames="payment_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntryEntity
{
    @Id
    @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(name="payment_id",nullable=false,unique=true)
    private UUID paymentId;

    @Column(name = "posted_at")
    private Instant postedAt;

    public LedgerEntryEntity(UUID paymentId)
    {
        this.paymentId=paymentId;
        this.postedAt=Instant.now();
    }

}
