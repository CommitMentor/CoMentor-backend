package com.knu.coment.repository;

import com.knu.coment.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long>{
    List<Answer> findAllByQuestionId(Long projectCsQuestionId);
    void deleteAllByQuestionId(Long projectCsQuestionId);
}
