package com.knu.coment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class UserNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String body;
    private boolean isRead= false;
    private LocalDateTime sentAt;

    public UserNotification(Long userId, String title, String body) {
        this.userId = userId;
        this.title = title;
        this.body = body;
        this.sentAt = LocalDateTime.now();
    }
    public void markAsRead() {
        this.isRead = true;
    }
}
