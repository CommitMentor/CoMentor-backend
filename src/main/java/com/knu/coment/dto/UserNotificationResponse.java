package com.knu.coment.dto;

import com.knu.coment.entity.UserNotification;

import java.time.LocalDateTime;

public record UserNotificationResponse(
        Long id,
        String title,
        String body,
        boolean read,
        LocalDateTime sentAt
) {
    public static UserNotificationResponse from(UserNotification notification) {
        return new UserNotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getBody(),
                notification.isRead(),
                notification.getSentAt()
        );
    }
}
