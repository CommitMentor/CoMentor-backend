package com.knu.coment.service;

import com.knu.coment.entity.User;
import com.knu.coment.entity.UserNotification;
import com.knu.coment.repository.UserNotificationRepository;
import com.knu.coment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final FcmService fcmService;
    private final UserRepository userRepository;
    private final UserNotificationRepository userNotificationRepository;

    @Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
    public void sendDailyNotifications() {
        String title = "📨오늘의 CS 질문 생성";
        String body = "지금 바로 도전해보세요!";
        List<User> users = userRepository.findByNotificationTrue();
        for (User user : users) {
            fcmService.sendToUser(user.getId(), title, body);
            userNotificationRepository.save(
                    new UserNotification(user.getId(), title, body)
            );
        }
    }
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul") // 매 시간마다 체크
    public void sendReminderToInactiveUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(48);
        List<User> inactiveUsers = userRepository.findInactiveSince(cutoff);

        for (User user : inactiveUsers) {
            if (Boolean.TRUE.equals(user.getNotification())) {
                fcmService.sendToUser(user.getId(), "⏰학습 리마인더", "학습을 다시 시작해보세요!");
            }
        }
    }
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul") // 매일 새벽 3시 실행
    public void deleteOldNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(4);
        userNotificationRepository.deleteBySentAtBefore(cutoff);
    }
}
