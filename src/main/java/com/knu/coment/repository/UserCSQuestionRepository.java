package com.knu.coment.repository;

import com.knu.coment.entity.UserCSQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserCSQuestionRepository extends JpaRepository<UserCSQuestion, Long> {

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

}

