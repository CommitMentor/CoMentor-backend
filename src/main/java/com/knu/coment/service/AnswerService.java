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
    private final UserService userService;
    private final GptService gptService;

    public Answer createAnswer(String githubId, Long csQuestionId, String answer) {
        userService.findByGithubId(githubId);
        CsQuestion csQuestion = csQuestionRepository.findById(csQuestionId)
                .orElseThrow(() -> new RuntimeException("CsQuestion not found with id: " + csQuestionId));
        Answer newAnswer = new Answer(
                answer,
                LocalDateTime.now(),
                Author.USER,
                csQuestion
        );
        answerRepository.save(newAnswer);

        String prompt = gptService.createPromptForAnswerProject(csQuestion.getUserCode(), csQuestion.getQuestion(), answer);
        String generatedAnswer = gptService.callGptApi(prompt);
        Answer newFeedback = new Answer(
                generatedAnswer,
                LocalDateTime.now(),
                Author.AI,
                csQuestion
        );
        return answerRepository.save(newFeedback);
    }
}

