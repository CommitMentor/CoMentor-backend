package com.knu.coment.repository;

import com.knu.coment.entity.UserStudyLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserStudyLogRepository extends JpaRepository<UserStudyLog, Long> {
    Optional<UserStudyLog> findByUserIdAndStudyDate(Long userId, LocalDate studyDate);
    List<UserStudyLog> findByUserIdAndStudyDateBetweenOrderByStudyDateDesc(Long userId, LocalDate startDate, LocalDate endDate);

    List<UserStudyLog> findByUserIdAndStudyDateLessThanEqualOrderByStudyDateDesc(Long userId, LocalDate endDate);

}
