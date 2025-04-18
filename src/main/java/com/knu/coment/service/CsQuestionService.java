package com.knu.coment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knu.coment.dto.gpt.*;
import com.knu.coment.entity.*;
import com.knu.coment.exception.ProjectException;
import com.knu.coment.exception.QuestionException;
import com.knu.coment.exception.code.ProjectErrorCode;
import com.knu.coment.exception.code.QuestionErrorCode;
import com.knu.coment.global.CSCategory;
import com.knu.coment.global.QuestionStatus;
import com.knu.coment.repository.AnswerRepository;
import com.knu.coment.repository.ProjectCsQuestionRepository;
import com.knu.coment.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CsQuestionService {
    private final UserService userService;
    private final GptService gptService;
    private final ProjectRepository projectRepository;
    private final ProjectCsQuestionRepository projectCsQuestionRepository;
    private final AnswerRepository answerRepository;

    private void validateProjectOwner(Long userId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.NOT_FOUND_PROJECT));
        if (!project.getUserId().equals(userId)) {
            throw new ProjectException(ProjectErrorCode.UNAUTHORIZED_ACCESS);
        }
    }
    public ProjectCsQuestion findById(Long projectQuestionId) {
        return projectCsQuestionRepository.findById(projectQuestionId)
                .orElseThrow(() -> new QuestionException(QuestionErrorCode.NOT_FOUND_QUESTION));
    }
    public List<ProjectCsQuestion> createProjectQuestions(String githubId, Long projectId, String userCode, String userCodeFolderName) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        User user = userService.findByGithubId(githubId);
        validateProjectOwner(user.getId(), projectId);
        String projectInfo = String.format("Project description: %s, project role: %s", project.getDescription(), project.getRole());
        String prompt = gptService.createPromptForProject(userCode, projectInfo);

        String generatedQuestions = gptService.callGptApi(prompt);

        generatedQuestions = stripCodeBlock(generatedQuestions);

        List<ProjectCsQuestion> savedQuestions = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            List<Map<String, String>> questionList = objectMapper.readValue(generatedQuestions,
                    new TypeReference<List<Map<String, String>>>(){});

            for (Map<String, String> questionMap : questionList) {
                String questionText = questionMap.get("question");
                String csCategory = questionMap.get("category");
                String relatedCode = questionMap.get("relatedCode");
                if (questionText != null && !questionText.trim().isEmpty()) {
                    ProjectCsQuestion projectCsQuestion = new ProjectCsQuestion(
                            CSCategory.valueOf((csCategory).trim()),
                            relatedCode,
                            questionText.trim(),
                            LocalDateTime.now(),
                            QuestionStatus.TODO,
                            userCodeFolderName,
                            null,
                            user.getId(),
                            project.getId()
                    );
                    savedQuestions.add(projectCsQuestionRepository.save(projectCsQuestion));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return savedQuestions;
    }
    private String stripCodeBlock(String input) {
        if(input == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("(?s)```(?:json)?\\s*(.*?)\\s*```");
        Matcher matcher = pattern.matcher(input.trim());
        if (matcher.find()) {
            return matcher.group(1);
        }
        return input;
    }
    public ProjectCsQuestionInfoResponse getCsQuestionDetail(String githubId, Long projectQuestionId) {
        ProjectCsQuestion question = findById(projectQuestionId);
        User user = userService.findByGithubId(githubId);
        validateProjectOwner(user.getId(), question.getProjectId());
        ProjectCsQuestion projectCsQuestion = findById(projectQuestionId);
        List<Answer> answers = answerRepository.findAllByProjectCsQuestionId(projectQuestionId);
        List<CreateFeedBackResponseDto> answerResponses = answers.stream()
                .sorted(Comparator.comparing(Answer::getAnsweredAt))
                .map(answer -> new CreateFeedBackResponseDto(
                        answer.getContent(),
                        answer.getAuthor().name()
                ))
                .collect(Collectors.toList());

        return new ProjectCsQuestionInfoResponse(
                projectCsQuestion.getId(),
                projectCsQuestion.getCsCategory(),
                projectCsQuestion.getRelatedCode(),
                projectCsQuestion.getQuestion(),
                projectCsQuestion.getQuestionStatus(),
                projectCsQuestion.getFolderName(),
                answerResponses
        );
    }

    public List<CsQuestionListDto> getGroupedCsQuestions(String githubId, Long projectId) {
        User user = userService.findByGithubId(githubId);
        validateProjectOwner(user.getId(), projectId);
        List<ProjectCsQuestion> questions = projectCsQuestionRepository.findAllByProjectId(projectId);

        return questions.stream()
                .collect(Collectors.groupingBy(q -> q.getCreateAt().toLocalDate()))
                .entrySet().stream()
                .map(entry -> new CsQuestionListDto(
                        entry.getKey(),
                        entry.getValue().stream()
                                .sorted(Comparator.comparing(ProjectCsQuestion::getId).reversed())
                                .map(q -> new ProjectQuestionListDto(q.getId(), q.getQuestion(),q.getFolderName(), q.getQuestionStatus()))
                                .collect(Collectors.toList())
                ))
                .sorted(Comparator.comparing(CsQuestionListDto::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
    @Transactional
    public void deleteProjectCsQuestion(Long projectCsQuestionId) {
        ProjectCsQuestion projectCsQuestion = projectCsQuestionRepository.findById(projectCsQuestionId)
                .orElseThrow(() -> new QuestionException(QuestionErrorCode.NOT_FOUND_QUESTION));
        Long csQuestionId = projectCsQuestion.getProjectId();
        answerRepository.deleteAllByProjectCsQuestionId(projectCsQuestionId);

        projectCsQuestionRepository.deleteById(csQuestionId);

        projectCsQuestionRepository.deleteById(projectCsQuestionId);
    }

}
