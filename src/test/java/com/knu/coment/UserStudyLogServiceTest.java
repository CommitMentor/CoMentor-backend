package com.knu.coment;

import com.knu.coment.dto.DailyStudyDto;
import com.knu.coment.entity.UserStudyLog;
import com.knu.coment.repository.UserStudyLogRepository;
import com.knu.coment.service.UserStudyLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class UserStudyLogServiceTest {

    private UserStudyLogRepository userStudyLogRepository;
    private UserStudyLogService userStudyLogService;

    @BeforeEach
    void setUp() {
        userStudyLogRepository = mock(UserStudyLogRepository.class);
        userStudyLogService = new UserStudyLogService(userStudyLogRepository);
    }

    @Test
    void updateSolvedCount_shouldCreateNewLogIfNotExists() {
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        when(userStudyLogRepository.findByUserIdAndStudyDate(userId, today))
                .thenReturn(Optional.empty());

        userStudyLogService.updateSolvedCount(userId);

        ArgumentCaptor<UserStudyLog> captor = ArgumentCaptor.forClass(UserStudyLog.class);
        verify(userStudyLogRepository).save(captor.capture());
        UserStudyLog saved = captor.getValue();

        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getStudyDate()).isEqualTo(today);
        assertThat(saved.getSolvedCount()).isEqualTo(1);
    }

    @Test
    void updateSolvedCount_shouldIncrementSolvedCountIfExists() {
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        UserStudyLog existingLog = UserStudyLog.builder()
                .userId(userId)
                .studyDate(today)
                .solvedCount(2)
                .build();

        when(userStudyLogRepository.findByUserIdAndStudyDate(userId, today))
                .thenReturn(Optional.of(existingLog));

        userStudyLogService.updateSolvedCount(userId);

        assertThat(existingLog.getSolvedCount()).isEqualTo(3);
        verify(userStudyLogRepository, never()).save(any());
    }

    @Test
    void getLast10DaysStudy_shouldReturnAllDatesWithCounts() {
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        List<UserStudyLog> logs = List.of(
                new UserStudyLog(userId, today.minusDays(1), 2),
                new UserStudyLog(userId, today.minusDays(3), 1)
        );
        when(userStudyLogRepository.findByUserIdAndStudyDateBetweenOrderByStudyDateDesc(
                eq(userId), any(), eq(today))
        ).thenReturn(logs);

        List<DailyStudyDto> result = userStudyLogService.getLast10DaysStudy(userId);

        assertThat(result).hasSize(10);
        assertThat(result.get(8).getSolvedCount()).isEqualTo(2); // today -1
        assertThat(result.get(6).getSolvedCount()).isEqualTo(1); // today -3
    }

    @Test
    void getStudyStreak_shouldCalculateConsecutiveDaysCorrectly() {
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        List<UserStudyLog> logs = List.of(
                new UserStudyLog(userId, today, 1),
                new UserStudyLog(userId, today.minusDays(1), 1),
                new UserStudyLog(userId, today.minusDays(2), 0),  // 끊김
                new UserStudyLog(userId, today.minusDays(3), 1)
        );

        when(userStudyLogRepository.findByUserIdAndStudyDateLessThanEqualOrderByStudyDateDesc(userId, today))
                .thenReturn(logs);

        int streak = userStudyLogService.getStudyStreak(userId);
        assertThat(streak).isEqualTo(2);
    }
}
