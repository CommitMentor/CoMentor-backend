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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserService userService;
    private final GptService gptService;
    private final UserCSQuestionRepository userCSQuestionRepository;
    private final UserStudyLogService userStudyLogService;

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
        Answer newAnswer = new Answer(
                answer,
                LocalDateTime.now(),
                Author.USER,
                questionId,
                user.getId()
        );
        answerRepository.save(newAnswer);
        userCSQuestion.markAsDone();
        userStudyLogService.updateSolvedCount(user.getId());
        userCSQuestionRepository.save(userCSQuestion);
        String prompt = gptService.createPromptForAnswerCS(projectCsQuestion.getStack(),projectCsQuestion.getCsCategory(),projectCsQuestion.getQuestion(), answer);
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
        userStudyLogService.updateSolvedCount(user.getId());
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
    @Transactional
    public Answer retryCsAnswer(String githubId, Long userCSQuestionId, String newAnswer) {
        User user = userService.findByGithubId(githubId);

        UserCSQuestion ucq = userCSQuestionRepository
                .findByIdAndUserId(userCSQuestionId, user.getId())
                .orElseThrow(() -> new AnswerException(AnswerErrorCode.NOT_RECOMMENDED_QUESTION));
        userStudyLogService.updateSolvedCount(user.getId());
        Question question = questionRepository.findById(ucq.getQuestionId())
                .orElseThrow(() -> new QuestionException(QuestionErrorCode.NOT_FOUND_QUESTION));

        return resolveAndSave(
                question,
                user.getId(),
                newAnswer,
                (q, ans) -> gptService.createPromptForAnswerCS(q.getStack(), q.getCsCategory(), q.getQuestion(), ans)
        );
    }

    @Transactional
    public Answer retryProjectAnswer(String githubId, Long csQuestionId, String newAnswer) {

        User user = userService.findByGithubId(githubId);
        userStudyLogService.updateSolvedCount(user.getId());
        Question question = questionRepository.findById(csQuestionId)
                .orElseThrow(() -> new QuestionException(QuestionErrorCode.NOT_FOUND_QUESTION));
        if (!user.getId().equals(question.getUserId())) {
            throw new AnswerException(AnswerErrorCode.UNAUTHORIZED_QUESTION_ACCESS);
        }

        return resolveAndSave(
                question,
                null,
                newAnswer,
                (q, ans) -> gptService.createPromptForAnswerProject(q.getRelatedCode(), q.getQuestion(), ans)
        );
    }
    private Answer resolveAndSave(
            Question question,
            Long userId,
            String newAnswer,
            BiFunction<Question, String, String> promptBuilder
    ) {
        Answer userAnswer = answerRepository
                .findByQuestionIdAndUserIdAndAuthor(
                        question.getId(), userId, Author.USER
                )
                .orElseThrow(() -> new AnswerException(AnswerErrorCode.NOT_FOUND_ANSWER));
        userAnswer.resolve(newAnswer);
        answerRepository.save(userAnswer);

        Answer aiFeedback = answerRepository
                .findByQuestionIdAndUserIdAndAuthor(
                        question.getId(), userId, Author.AI
                )
                .orElseThrow(() -> new AnswerException(AnswerErrorCode.NOT_FOUND_ANSWER));

        String prompt = promptBuilder.apply(question, newAnswer);
        String generated = gptService.callGptApi(prompt);
        String feedback = getFeedback(generated);

        aiFeedback.resolve(feedback);
        return answerRepository.save(aiFeedback);
    }

    private String getFeedback(String input) {
        String sanitized = input
                .replaceAll("(?m)^```(?:json)?\\s*", "")
                .replaceAll("(?m)```\\s*$", "")
                .trim();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, String>> list = objectMapper.readValue(
                    sanitized,
                    new TypeReference<List<Map<String, String>>>() {}
            );
            return list.isEmpty() ? "" : list.get(0).getOrDefault("feedback", "");
        } catch (Exception e) {
            throw new AnswerException(AnswerErrorCode.FEEDBACK_PARSE_ERROR);
        }
    }


}

