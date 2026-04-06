package com.harsha.notification_service.infrastructure.worker;

import com.harsha.notification_service.domain.model.entities.NotificationDelivery;
import com.harsha.notification_service.domain.repository.NotificationDeliveryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeliveryWorker {
    private final NotificationDeliveryRepository deliveryRepository;
    private final DeliveryProcessor deliveryProcessor;

    public DeliveryWorker(
            NotificationDeliveryRepository deliveryRepository,
            DeliveryProcessor deliveryProcessor
    ) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryProcessor = deliveryProcessor;
    }

    @Scheduled(fixedRate = 10000)
    public void processDelivery() {
        List<NotificationDelivery> deliveries =
                deliveryRepository.lockNextBatch("PENDING",50);

        for (NotificationDelivery delivery : deliveries) {
            deliveryProcessor.processingSingleDelivery(delivery.getId());
        }
    }


}
