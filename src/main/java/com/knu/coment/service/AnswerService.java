package com.knu.coment.service;

import com.knu.coment.entity.Answer;
import com.knu.coment.entity.CsQuestion;
import com.knu.coment.global.Author;
import com.knu.coment.repository.AnswerRepository;
import com.knu.coment.repository.CsQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final CsQuestionRepository csQuestionRepository;

    public Answer createAnswer(Long csQuestionId, String content, Author author) {
        CsQuestion csQuestion = csQuestionRepository.findById(csQuestionId)
                .orElseThrow(() -> new RuntimeException("CsQuestion not found with id: " + csQuestionId));

        Answer newAnswer = new Answer(
                content,
                LocalDateTime.now(),
                author,
                csQuestion
        );
        return answerRepository.save(newAnswer);
    }
}

