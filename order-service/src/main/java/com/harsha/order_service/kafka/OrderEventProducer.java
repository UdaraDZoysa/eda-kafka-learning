package com.harsha.order_service.kafka;

import com.harsha.order_service.events.OrderPlacedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventProducer {
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private final String topicName;

    public OrderEventProducer(
            KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate,
            @Value("${topic.order}") String topicName
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }
    public void publish(OrderPlacedEvent event) {
        kafkaTemplate.send(topicName, event.orderId(), event);

        System.out.println(
                "Published OrderPlacedEvent → orderId=" + event.orderId()
        );
    }
}
