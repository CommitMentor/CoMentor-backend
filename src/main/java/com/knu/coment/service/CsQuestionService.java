package com.knu.coment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knu.coment.dto.gpt.CsQuestionAnswerResponse;
import com.knu.coment.dto.gpt.ProjectCsQuestionInfoResponse;
import com.knu.coment.entity.Answer;
import com.knu.coment.entity.CsQuestion;
import com.knu.coment.entity.Project;
import com.knu.coment.entity.User;
import com.knu.coment.repository.CsQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    private final ProjectService projectService;
    private final GptService gptService;
    private final CsQuestionRepository csQuestionRepository;

    public List<CsQuestion> createProjectQuestions(String githubId, Long projectId, String userCode) {
        User user = userService.findByGithubId(githubId);
        Project project = projectService.findById(projectId);

        String projectInfo = String.format("Project description: %s, project role: %s",
                project.getDescription(), project.getRole());
        String prompt = gptService.createPromptForProject(userCode, projectInfo);

        String generatedQuestions = gptService.callGptApi(prompt);

        generatedQuestions = stripCodeBlock(generatedQuestions);

        List<CsQuestion> savedQuestions = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            List<Map<String, String>> questionList = objectMapper.readValue(generatedQuestions,
                    new TypeReference<List<Map<String, String>>>(){});

            for (Map<String, String> questionMap : questionList) {
                String questionText = questionMap.get("question");
                if (questionText != null && !questionText.trim().isEmpty()) {
                    CsQuestion csQuestion = new CsQuestion(
                            userCode,
                            questionText.trim(),
                            LocalDateTime.now(),
                            user,
                            project
                    );
                    savedQuestions.add(csQuestionRepository.save(csQuestion));
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
    public ProjectCsQuestionInfoResponse getCsQuestionDetail(String githubId, Long questionId) {
        userService.findByGithubId(githubId);
        CsQuestion csQuestion = csQuestionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("CsQuestion not found"));

        List<CsQuestionAnswerResponse> answerResponses = csQuestion.getAnswer().stream()
                .sorted(Comparator.comparing(Answer::getAnsweredAt))
                .map(answer -> new CsQuestionAnswerResponse(
                        answer.getContent(),
                        answer.getAnsweredAt(),
                        answer.getAuthor().name()
                ))
                .collect(Collectors.toList());

        return new ProjectCsQuestionInfoResponse(
                csQuestion.getId(),
                csQuestion.getUserCode(),
                csQuestion.getQuestion(),
                csQuestion.getCreateAt(),
                answerResponses
        );
    }
}
