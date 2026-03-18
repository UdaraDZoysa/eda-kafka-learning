package com.harsha.payment_service.infrastructure.messaging;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {
    @Value("topic.order.dlt")
    private String dltTopic;

    public DefaultErrorHandler errorHandler(
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, ex) ->
                                new TopicPartition(dltTopic, record.partition())
                );
        FixedBackOff backOff = new FixedBackOff(2000L, 3);
        return new DefaultErrorHandler(recoverer, backOff);
    }
}
