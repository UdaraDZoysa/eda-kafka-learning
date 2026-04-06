package com.harsha.notification_service.infrastructure.provider;

public interface NotificationSender {
    void sendNotification(String userId, String message);
}
