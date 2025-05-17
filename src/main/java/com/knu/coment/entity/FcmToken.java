package com.knu.coment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class FcmToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String fcmToken;
    public FcmToken(Long userId, String fcmToken) {
        this.userId = userId;
        this.fcmToken = fcmToken;
    }
    public void updateUserId(Long userId) {
        this.userId = userId;
    }
}
