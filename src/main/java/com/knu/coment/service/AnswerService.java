package com.knu.coment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knu.coment.entity.Answer;
import com.knu.coment.entity.ProjectCsQuestion;
import com.knu.coment.exception.AnswerExceptionHandler;
import com.knu.coment.exception.QuestionExceptionHandler;
import com.knu.coment.exception.code.AnswerErrorCode;
import com.knu.coment.exception.code.QuestionErrorCode;
import com.knu.coment.global.Author;
import com.knu.coment.global.QuestionStatus;
import com.knu.coment.repository.AnswerRepository;
import com.knu.coment.repository.ProjectCsQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final ProjectCsQuestionRepository projectCsQuestionRepository;
    private final UserService userService;
    private final GptService gptService;

    public Answer createAnswer(String githubId, Long csQuestionId, String answer) {
        userService.findByGithubId(githubId);
        ProjectCsQuestion projectCsQuestion = projectCsQuestionRepository.findById(csQuestionId)
                .orElseThrow(() -> new QuestionExceptionHandler(QuestionErrorCode.NOT_FOUND_QUESTION));
        if(projectCsQuestion.getQuestionStatus() == QuestionStatus.DONE) {
            throw new AnswerExceptionHandler(AnswerErrorCode.ALREADY_DONE_ANSWER);
        }
        Answer newAnswer = new Answer(
                answer,
                LocalDateTime.now(),
                Author.USER,
                csQuestionId
        );
        answerRepository.save(newAnswer);
        projectCsQuestion.markAsDone();
        projectCsQuestionRepository.save(projectCsQuestion);
        String prompt = gptService.createPromptForAnswerProject(projectCsQuestion.getRelatedCode(), projectCsQuestion.getQuestion(), answer);
        String generatedAnswer = gptService.callGptApi(prompt);
        generatedAnswer = getFeedback(generatedAnswer);
        Answer newFeedback = new Answer(
                generatedAnswer,
                LocalDateTime.now(),
                Author.AI,
                csQuestionId
        );
        return answerRepository.save(newFeedback);
    }
    private String getFeedback(String input){
        String feedback = "";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, String>> result = objectMapper.readValue(input,
                    new TypeReference<List<Map<String, String>>>(){});
            if (!result.isEmpty() && result.get(0).containsKey("feedback")) {
                feedback = result.get(0).get("feedback");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return feedback;
    }
}

