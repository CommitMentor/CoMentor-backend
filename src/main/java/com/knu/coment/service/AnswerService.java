package com.knu.coment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knu.coment.entity.Answer;
import com.knu.coment.entity.Question;
import com.knu.coment.entity.User;
import com.knu.coment.entity.UserCSQuestion;
import com.knu.coment.exception.AnswerException;
import com.knu.coment.exception.QuestionException;
import com.knu.coment.exception.code.AnswerErrorCode;
import com.knu.coment.exception.code.QuestionErrorCode;
import com.knu.coment.global.Author;
import com.knu.coment.global.QuestionStatus;
import com.knu.coment.repository.AnswerRepository;
import com.knu.coment.repository.UserCSQuestionRepository;
import com.knu.coment.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserService userService;
    private final GptService gptService;
    private final UserCSQuestionRepository userCSQuestionRepository;

    public Answer createCSAnswer(String githubId, Long userCSQuestionId, String answer) {
        User user = userService.findByGithubId(githubId);
        UserCSQuestion userCSQuestion = userCSQuestionRepository.findByIdAndUserId(userCSQuestionId, user.getId())
                .orElseThrow(() -> new AnswerException(AnswerErrorCode.NOT_RECOMMENDED_QUESTION));
        if (userCSQuestion.getQuestionStatus() == QuestionStatus.DONE) {
            throw new AnswerException(AnswerErrorCode.ALREADY_DONE_ANSWER);
        }
        Long questionId = userCSQuestion.getQuestionId();
        Question projectCsQuestion = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionException(QuestionErrorCode.NOT_FOUND_QUESTION));
        if(answerRepository.findByUserIdAndQuestionId(user.getId(), questionId).isPresent()) {
            throw new AnswerException(AnswerErrorCode.ALREADY_DONE_ANSWER);
        }
        Answer newAnswer = new Answer(
                answer,
                LocalDateTime.now(),
                Author.USER,
                questionId,
                user.getId()
        );
        answerRepository.save(newAnswer);
        String prompt = gptService.createPromptForAnswerCS(projectCsQuestion.getCsCategory(),projectCsQuestion.getQuestion(), answer);
        String generatedAnswer = gptService.callGptApi(prompt);
        generatedAnswer = getFeedback(generatedAnswer);
        Answer newFeedback = new Answer(
                generatedAnswer,
                LocalDateTime.now(),
                Author.AI,
                questionId,
                user.getId()
        );
        return answerRepository.save(newFeedback);
    }
    public Answer createAnswer(String githubId, Long csQuestionId, String answer) {
        User user = userService.findByGithubId(githubId);
        Question projectCsQuestion = questionRepository.findById(csQuestionId)
                .orElseThrow(() -> new QuestionException(QuestionErrorCode.NOT_FOUND_QUESTION));
        if (!user.getId().equals(projectCsQuestion.getUserId())) {
            throw new AnswerException(
                    AnswerErrorCode.UNAUTHORIZED_QUESTION_ACCESS);
        }
        if(projectCsQuestion.getQuestionStatus() == QuestionStatus.DONE) {
            throw new AnswerException(AnswerErrorCode.ALREADY_DONE_ANSWER);
        }
        Answer newAnswer = new Answer(
                answer,
                LocalDateTime.now(),
                Author.USER,
                csQuestionId,
                null
        );
        answerRepository.save(newAnswer);
        projectCsQuestion.markAsDone();
        questionRepository.save(projectCsQuestion);
        String prompt = gptService.createPromptForAnswerProject(projectCsQuestion.getRelatedCode(), projectCsQuestion.getQuestion(), answer);
        String generatedAnswer = gptService.callGptApi(prompt);
        generatedAnswer = getFeedback(generatedAnswer);
        Answer newFeedback = new Answer(
                generatedAnswer,
                LocalDateTime.now(),
                Author.AI,
                csQuestionId,
                null
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

