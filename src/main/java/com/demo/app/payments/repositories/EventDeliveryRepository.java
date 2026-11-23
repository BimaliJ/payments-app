package com.demo.app.payments.repositories;

import com.demo.app.payments.model.enums.DeliveryStatus;
import com.demo.app.payments.model.entities.EventDeliveryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface EventDeliveryRepository extends JpaRepository<EventDeliveryEntity, UUID>
{
    @Query("select d from EventDeliveryEntity d where d.status in :statuses and (d.nextRetryAt is null or d.nextRetryAt <= :now) order by d.createdAt")
    List<EventDeliveryEntity> findReady(@Param("statuses") Collection<DeliveryStatus> statuses, @Param("now") Instant now, Pageable pageable);

}
