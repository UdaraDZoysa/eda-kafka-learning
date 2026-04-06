package com.harsha.notification_service.application.service;

import com.harsha.common.events.PaymentProcessedEvent;
import com.harsha.notification_service.domain.model.entities.Notification;
import com.harsha.notification_service.domain.model.entities.NotificationDelivery;
import com.harsha.notification_service.domain.model.enums.NotificationChannel;
import com.harsha.notification_service.domain.repository.NotificationDeliveryRepository;
import com.harsha.notification_service.domain.repository.NotificationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryRepository deliveryRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            NotificationDeliveryRepository deliveryRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.deliveryRepository = deliveryRepository;
    }

    public void handlePaymentResult(PaymentProcessedEvent event, UUID inboxEventId) {
        String message;

        if (event.success()) {
            message = "Payment successful for order " + event.orderId();
        } else {
            message = "Payment failed for order " + event.orderId();
        }

        Notification notification =
                new Notification(
                        event.userId(),
                        event.orderId(),
                        inboxEventId,
                        message,
                        event.success()
                );

        try {
            notificationRepository.save(notification);
        } catch (DataIntegrityViolationException ex) {
            return;
        }

        //For now Just send as EMAIL
        NotificationDelivery delivery =
                new NotificationDelivery(
                        notification.getId(),
                        NotificationChannel.EMAIL
                );

        deliveryRepository.save(delivery);
    };
}
