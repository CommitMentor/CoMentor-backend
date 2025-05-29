package com.knu.coment.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.knu.coment.entity.FcmToken;
import com.knu.coment.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmService {
    private final FcmTokenRepository fcmTokenRepository;

    public void saveOrUpdateFcmToken(Long userId, String fcmToken) {
        fcmTokenRepository.findByFcmToken(fcmToken)
                .ifPresentOrElse(
                        token -> token.updateUserId(userId),
                        () -> fcmTokenRepository.save(new FcmToken(userId, fcmToken))
                );
    }
    public void sendToUser(Long userId, String title, String body, String type, String url) {
        List<FcmToken> tokens = fcmTokenRepository.findByUserId(userId);
        for (FcmToken token : tokens) {
            send(token.getFcmToken(), title, body, type, url);
        }
    }

    private void send(String token, String title, String body, String type, String url) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .putData("title", title)
                    .putData("body", body)
                    .putData("type", type)
                    .putData("url", url)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            System.err.println("FCM 실패: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteToken(String token) {

        fcmTokenRepository.deleteByFcmToken(token);
    }

}
