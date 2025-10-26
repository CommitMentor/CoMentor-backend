package com.knu.coment;

import com.knu.coment.entity.User;
import com.knu.coment.entity.UserNotification;
import com.knu.coment.global.Role;
import com.knu.coment.repository.UserNotificationRepository;
import com.knu.coment.repository.UserRepository;
import com.knu.coment.service.FcmService;
import com.knu.coment.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private FcmService fcmService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserNotificationRepository userNotificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user(long id, boolean notificationEnabled, LocalDateTime lastActivity) {
        User u = User.builder()
                .id(id)
                .userName("user" + id)
                .githubId("gh" + id)
                .email("user" + id + "@example.com")
                .notification(notificationEnabled)
                .userRole(Role.USER)
                .avatarUrl("https://example.com/avatar" + id + ".png")
                .build();
        if (lastActivity != null) {
            // User.updateLastActivityAt() sets 'now', so set via reflection
            u.updateLastActivityAt();
            // overwrite with supplied timestamp
            try {
                var field = User.class.getDeclaredField("lastActivityAt");
                field.setAccessible(true);
                field.set(u, lastActivity);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        return u;
    }

    @Test
    @DisplayName("sendDailyNotifications - 알림 허용 유저에게 FCM과 UserNotification을 저장한다")
    void sendDailyNotifications_notifiesAllSubscribedUsers() {
        User user1 = user(1L, true, null);
        User user2 = user(2L, true, null);
        given(userRepository.findByNotificationTrue()).willReturn(List.of(user1, user2));

        notificationService.sendDailyNotifications();

        verify(fcmService).sendToUser(1L, "📨오늘의 CS 질문 생성", "지금 바로 도전해보세요!", "DAILY", "/question/list");
        verify(fcmService).sendToUser(2L, "📨오늘의 CS 질문 생성", "지금 바로 도전해보세요!", "DAILY", "/question/list");

        ArgumentCaptor<UserNotification> captor = ArgumentCaptor.forClass(UserNotification.class);
        verify(userNotificationRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).hasSize(2);
        assertThat(captor.getAllValues())
                .allSatisfy(notification -> {
                    assertThat(notification.getTitle()).isEqualTo("📨오늘의 CS 질문 생성");
                    assertThat(notification.getBody()).isEqualTo("지금 바로 도전해보세요!");
                    assertThat(notification.getUserId()).isIn(1L, 2L);
                });
    }

    @Test
    @DisplayName("sendDailyNotifications - 대상 유저가 없으면 호출이 발생하지 않는다")
    void sendDailyNotifications_noSubscribersDoesNothing() {
        given(userRepository.findByNotificationTrue()).willReturn(List.of());

        notificationService.sendDailyNotifications();

        verify(fcmService, never()).sendToUser(anyLong(), anyString(), anyString(), anyString(), anyString());
        verify(userNotificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("sendReminderToInactiveUsers - 48시간 비활성 사용자 중 알림 허용자만 전송한다")
    void sendReminderToInactiveUsers_notifiesOnlyOptInUsers() {
        User inactiveWithNotification = user(10L, true, LocalDateTime.now().minusHours(60));
        User inactiveWithoutNotification = user(11L, false, LocalDateTime.now().minusHours(60));

        given(userRepository.findInactiveSince(any(LocalDateTime.class)))
                .willReturn(List.of(inactiveWithNotification, inactiveWithoutNotification));

        notificationService.sendReminderToInactiveUsers();

        ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(userRepository).findInactiveSince(cutoffCaptor.capture());
        LocalDateTime expectedCutoff = LocalDateTime.now().minusHours(48);
        assertThat(Duration.between(expectedCutoff, cutoffCaptor.getValue()).abs()).isLessThan(Duration.ofSeconds(5));

        verify(fcmService).sendToUser(10L, "⏰학습 리마인더", "학습을 다시 시작해보세요!", "REMINDER", "/project");
        verify(fcmService, never()).sendToUser(eq(11L), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("deleteOldNotifications - 기준시각 이전 데이터를 제거한다")
    void deleteOldNotifications_prunesEntriesOlderThanFourDays() {
        notificationService.deleteOldNotifications();

        ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(userNotificationRepository).deleteBySentAtBefore(cutoffCaptor.capture());
        LocalDateTime expected = LocalDateTime.now().minusDays(4);
        assertThat(Duration.between(expected, cutoffCaptor.getValue()).abs()).isLessThan(Duration.ofSeconds(5));
    }
}
