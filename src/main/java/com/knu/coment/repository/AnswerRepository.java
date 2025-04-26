package com.knu.coment.repository;

import com.knu.coment.entity.Answer;
import com.knu.coment.global.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long>{
    List<Answer> findAllByQuestionId(Long projectCsQuestionId);
    void deleteAllByQuestionId(Long projectCsQuestionId);
    Optional<Answer> findByUserIdAndQuestionId(Long userId, Long questionId);
}
