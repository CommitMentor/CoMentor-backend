package com.knu.coment.service;

import com.knu.coment.dto.DailyStudyDto;
import com.knu.coment.entity.UserStudyLog;
import com.knu.coment.repository.UserStudyLogRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserStudyLogService {
    private final UserStudyLogRepository userStudyLogRepository;

    @Transactional
    public void updateSolvedCount(Long userId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        Optional<UserStudyLog> optionalLog = userStudyLogRepository.findByUserIdAndStudyDate(userId, today);

        if (optionalLog.isPresent()) {
            UserStudyLog log = optionalLog.get();
            log.updateSolvedCount(log.getSolvedCount() + 1);
        } else {
            UserStudyLog log = UserStudyLog.builder()
                    .userId(userId)
                    .studyDate(today)
                    .solvedCount(1)
                    .build();
            userStudyLogRepository.save(log);
        }
    }

    public List<DailyStudyDto> getLast10DaysStudy(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(9);

        List<UserStudyLog> logs = userStudyLogRepository
                .findByUserIdAndStudyDateBetweenOrderByStudyDateDesc(userId, startDate, today);

        Map<LocalDate, Integer> logMap = logs.stream()
                .collect(Collectors.toMap(UserStudyLog::getStudyDate, UserStudyLog::getSolvedCount));

        List<DailyStudyDto> result = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            LocalDate date = today.minusDays(i);
            int solvedCount = logMap.getOrDefault(date, 0);
            result.add(new DailyStudyDto(date, solvedCount));
        }
        Collections.reverse(result);
        return result;
    }

    public int getStudyStreak(Long userId) {
        LocalDate today = LocalDate.now();

        List<UserStudyLog> logs = userStudyLogRepository
                .findByUserIdAndStudyDateLessThanEqualOrderByStudyDateDesc(userId, today);

        int streak = 0;
        LocalDate current = today;

        for (UserStudyLog log : logs) {
            if (log.getStudyDate().isEqual(current) && log.getSolvedCount() > 0) {
                streak++;
                current = current.minusDays(1);
            } else if (log.getStudyDate().isBefore(current)) {
                break; // 연속성 끊김
            } else {
                continue;
            }
        }

        return streak;
    }



}
