package com.harsha.notification_service.infrastructure.provider.Sender;

import com.harsha.notification_service.infrastructure.provider.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);
    @Override
    public void sendNotification(String userId, String message) {
        //Just Demo for now
        System.out.println(
                "Sending EMAIL to user " + userId + " : " + message
        );
        log.info("Sending EMAIL to user " + userId + " : " + message);
    }
}
