
package com.demo.app.payments.mapper;

import com.demo.app.payments.model.DestinationAccountResponse;
import com.demo.app.payments.model.PaymentRequest;
import com.demo.app.payments.model.PaymentResponse;
import com.demo.app.payments.model.entities.PaymentEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "destinationAccountName", source = "destinationAccount.name")
    @Mapping(target = "destinationAccountNumber", source = "destinationAccount.accountNumber")
    @Mapping(target = "status", constant = "SUBMITTED")
    @Mapping(target = "fraudDecision", constant = "PENDING")
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    PaymentEntity toEntity(PaymentRequest request);

    @Mapping(target = "destinationAccount", expression = "java(toDestinationAccount(paymentEntity))")
    @Mapping(target = "fraudReasons", expression = "java(toFraudReasons(paymentEntity.getFraudReasons()))")
    PaymentResponse toResponse(PaymentEntity paymentEntity);

    default DestinationAccountResponse toDestinationAccount(PaymentEntity entity) {
        return new DestinationAccountResponse(
                entity.getDestinationAccountName(),
                entity.getDestinationAccountNumber()
        );
    }

    default java.util.List<String> toFraudReasons(String reasons) {
        if (reasons == null || reasons.isBlank()) {
            return java.util.List.of();
        }
        return java.util.Arrays.asList(reasons.split("; ", -1));
    }
}
