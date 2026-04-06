package com.harsha.notification_service.infrastructure.worker;

import com.harsha.notification_service.domain.model.entities.DeliveryAttempt;
import com.harsha.notification_service.domain.model.entities.Notification;
import com.harsha.notification_service.domain.model.entities.NotificationDelivery;
import com.harsha.notification_service.domain.repository.DeliveryAttemptRepository;
import com.harsha.notification_service.domain.repository.NotificationDeliveryRepository;
import com.harsha.notification_service.domain.repository.NotificationRepository;
import com.harsha.notification_service.infrastructure.provider.NotificationSender;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeliveryProcessor {
    private final NotificationDeliveryRepository deliveryRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationSender sender;
    private final DeliveryAttemptRepository attemptRepository;
    private static final Logger log = LoggerFactory.getLogger(DeliveryProcessor.class);

    public DeliveryProcessor(
            NotificationDeliveryRepository deliveryRepository,
            NotificationRepository notificationRepository,
            NotificationSender sender,
            DeliveryAttemptRepository attemptRepository
    ) {
        this.deliveryRepository = deliveryRepository;
        this.notificationRepository = notificationRepository;
        this.sender = sender;
        this.attemptRepository = attemptRepository;
    }

    @Transactional
    public void processingSingleDelivery(UUID deliveryId) {
        NotificationDelivery delivery = deliveryRepository
                .findById(deliveryId)
                .orElse(null);

        if (delivery == null) {
            return;
        }

        Notification notification = notificationRepository
                .findById(delivery.getNotificationId())
                .orElse(null);

        if (notification == null) {
            markAsFailed(delivery, "Notification Not Found");
            return;
        }

        try {
            sender.sendNotification(
                    notification.getUserId(),
                    notification.getMessage()
            );

            delivery.markSuccess();
            notification.markSent();

            attemptRepository.save(
                    new DeliveryAttempt(
                            delivery.getId(),
                            delivery.getRetryCount(),
                            true,
                            "Delivery Success"
                    )
            );
            deliveryRepository.save(delivery);
            notificationRepository.save(notification);
        } catch (Exception e) {
            handleRetry(delivery, notification, e);
        }
    }

    private long calculateBackoff(int retryCount) {
        long baseDelay = (long) Math.min(60000, Math.pow(2, retryCount) * 1000);
        double jitter = 0.5 + Math.random();
        return (long) (baseDelay * jitter);
    }

    private void markAsFailed(NotificationDelivery delivery, String message) {
        delivery.markFailed();
        deliveryRepository.save(delivery);
        attemptRepository.save(
                new DeliveryAttempt(
                        delivery.getId(),
                        delivery.getRetryCount(),
                        false,
                        message
                )
        );
    }

    private void handleRetry(
            NotificationDelivery delivery,
            Notification notification,
            Exception ex
    ) {
        long backoff = calculateBackoff(delivery.getRetryCount());

        delivery.markFailureAttempt(backoff);

        //Retries should record for future audits
        attemptRepository.save(
                new DeliveryAttempt(
                        delivery.getId(),
                        delivery.getRetryCount(),
                        false,
                        ex.getMessage()
                )
        );

        if (!delivery.shouldRetry()) {
            markAsFailed(delivery, ex.getMessage());
            notification.markFailed();
            notificationRepository.save(notification);
            log.warn(
                    "Delivery failed for notification {} retry={} reason={}",
                    delivery.getNotificationId(),
                    delivery.getRetryCount(),
                    ex.getMessage()
            );
        }
    }
}
