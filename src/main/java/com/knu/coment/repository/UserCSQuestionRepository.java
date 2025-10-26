package com.knu.coment.repository;

import com.knu.coment.dto.CategoryCorrectCountDto;
import com.knu.coment.entity.UserCSQuestion;
import com.knu.coment.global.CSCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserCSQuestionRepository extends JpaRepository<UserCSQuestion, Long> {

    List<UserCSQuestion> findAllByUserIdAndFileId(Long userId, Long fileId);
    boolean existsByUserIdAndDate(Long userId, LocalDate date);

    Page<UserCSQuestion> findAllByUserId(Long userId, Pageable pageable);

    Optional<UserCSQuestion> findByIdAndUserId(Long id, Long userId);
    @Query("""
    select ucq.questionId
    from UserCSQuestion ucq
    where ucq.userId = :userId
      and ucq.questionStatus = 'DONE'
""")
    List<Long> findSolvedQuestionIdsByUserId(@Param("userId") Long userId);
    List<UserCSQuestion> findAllByUserIdAndFileName(Long userId, String fileName);

    @Query("SELECT ucq FROM UserCSQuestion ucq " +
            "JOIN Question q ON ucq.questionId = q.id " +
            "WHERE ucq.userId = :userId AND q.csCategory = :category")
    Page<UserCSQuestion> findByUserIdAndCategory(@Param("userId") Long userId,
                                                 @Param("category") CSCategory category,
                                                 Pageable pageable);

    @Query("SELECT ucq.questionId FROM UserCSQuestion ucq WHERE ucq.userId = :userId")
    List<Long> findAllQuestionIdsByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserCSQuestion ucq WHERE ucq.userId = :userId AND ucq.questionStatus = 'TODO' AND ucq.date < :expiryDate")
    void deleteOldUnsolvedQuestions(@Param("userId") Long userId, @Param("expiryDate") LocalDate expiryDate);

    @Query("SELECT q.csCategory, COUNT(uq) " +
            "FROM UserCSQuestion uq " +
            "JOIN Question q ON uq.questionId = q.id " +
            "WHERE uq.userId = :userId AND uq.questionStatus = 'DONE' " +
            "GROUP BY q.csCategory")
    List<Object[]> countSolvedByCategory(@Param("userId") Long userId);

    @Query("""
    SELECT new com.knu.coment.dto.CategoryCorrectCountDto(
        q.csCategory,
        SUM(CASE WHEN ucq.isCorrect = true THEN 1 ELSE 0 END),
        SUM(CASE WHEN ucq.isCorrect = false THEN 1 ELSE 0 END)
    )
    FROM UserCSQuestion ucq
    JOIN Question q ON ucq.questionId = q.id
    WHERE ucq.userId = :userId
    GROUP BY q.csCategory
""")
    List<CategoryCorrectCountDto> countCorrectAndIncorrectByCategory(@Param("userId") Long userId);



}

