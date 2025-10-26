package com.knu.coment;

import com.knu.coment.entity.User;
import com.knu.coment.repository.UserNotificationRepository;
import com.knu.coment.repository.UserRepository;
import com.knu.coment.service.FcmService;
import com.knu.coment.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private FcmService fcmService;
    @Mock private UserRepository userRepository;
    @Mock private UserNotificationRepository userNotificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void sendDailyNotifications_sendsToAllUsersWithNotificationTrue() {

        User user1 = User.builder()
                .id(1L)
                .githubId("user1@test.com")
                .email("test@example.com")
                .build();
        User user2 = User.builder()
                .id(2L)
                .githubId("user2@test.com")
                .email("test@example.com")
                .build();
        given(userRepository.findByNotificationTrue()).willReturn(List.of(user1, user2));

        // when
        notificationService.sendDailyNotifications();

        // then
        verify(fcmService).sendToUser(eq(1L), anyString(), anyString());
        verify(fcmService).sendToUser(eq(2L), anyString(), anyString());
        verify(userNotificationRepository, times(2)).save(any());
    }

    @Test
    void deleteOldNotifications_removesNotificationsOlderThan4Days() {
        // when
        notificationService.deleteOldNotifications();

        // then
        verify(userNotificationRepository).deleteBySentAtBefore(any());
    }
}

