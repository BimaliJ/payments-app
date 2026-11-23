package com.demo.app.payments.consumer;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Log4j2
public class NotificationConsumer
{
    public void accept(UUID paymentId)
    {
        log.info("Notification sent for payment : {}",
                paymentId);
    }
}
