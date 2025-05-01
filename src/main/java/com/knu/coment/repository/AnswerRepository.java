package com.knu.coment.repository;

import com.knu.coment.entity.Answer;
import com.knu.coment.global.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long>{
    List<Answer> findAllByQuestionId(Long projectCsQuestionId);
    void deleteAllByQuestionId(Long projectCsQuestionId);
    List<Answer> findAllByUserIdAndQuestionId(Long userId, Long questionId);
    Optional<Answer> findByQuestionIdAndUserIdAndAuthor(Long questionId, Long userId, Author author);
}
