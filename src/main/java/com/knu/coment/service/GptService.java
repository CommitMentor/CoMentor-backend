package com.knu.coment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knu.coment.config.GptConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GptService {

    private final GptConfig gptConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String callGptApi(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", gptConfig.getModel(),
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );


        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, gptConfig.httpHeaders());
        String gptApiUrl = "https://api.openai.com/v1/chat/completions";
        RestTemplate restTemplate = gptConfig.restTemplate();

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(gptApiUrl, requestEntity, String.class);
        String responseBody = responseEntity.getBody();

        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode choicesNode = rootNode.path("choices");
            if (choicesNode.isArray() && choicesNode.size() > 0) {
                return choicesNode.get(0).path("message").path("content").asText().trim();
            } else {
                return "No response from GPT API.";
            }
        } catch (Exception e) {
            return "Error processing GPT API response.";
        }

    }

    public String createPromptForProject(String userCode, String projectInfo) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("당신은 소프트웨어 엔지니어입니다. 아래에 제공된 코드와 프로젝트 정보를 기반으로, ");
        prompt.append("CS 개념, 데이터 활용 방식등을 종합해 ");
        prompt.append("3개의 한국어 질문을 생성하세요.\n\n");


        prompt.append("아래는 코드와 프로젝트 정보입니다.\n\n");
        prompt.append("코드:\n").append(userCode).append("\n\n");
        prompt.append("프로젝트 정보:\n").append(projectInfo).append("\n\n");

        prompt.append("위 내용을 종합하여, 아래와 같은 JSON 배열 형식으로 3개의 질문을 생성하세요.\n");
        prompt.append("출력 예시:\n");
        prompt.append("[\n");
        prompt.append("  { \"question\": \"첫 번째 질문 내용\"},\n");
        prompt.append("  { \"question\": \"두 번째 질문 내용\"},\n");
        prompt.append("  { \"question\": \"세 번째 질문 내용\"}\n");
        prompt.append("]\n");

        return prompt.toString();
    }

    public String createPromptForAnswerProject(String userCode, String csQuestion, String answer) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("다음 코드와 질문에 대해 사용자가 답변한 내용입니다. 이 내용에 대해서 피드백이나 답변이 없을 시 알맞은 한국어 해답을 제공하세요\n");
        prompt.append("This is the code: ").append(userCode).append("\n");
        prompt.append("This is the question").append(csQuestion).append("\n");
        prompt.append("this is the answer: ").append(answer).append("\n");
        return prompt.toString();
    }
}


