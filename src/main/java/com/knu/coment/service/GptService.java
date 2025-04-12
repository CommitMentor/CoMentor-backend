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

        prompt.append("당신은 10년 경력의 시니어 소프트웨어 엔지니어이자 면접관입니다. ");
        prompt.append("아래에 제공된 코드만 보고, 코드에 나타난 프로그래밍 언어와 기술 스택, 핵심 CS 개념(OOP, 자료구조, DB, 네트워크 등)을 파악해 ");
        prompt.append("해당 코드와 연관된 CS 면접 질문을 3개를 한국어로 생성해주세요.\n\n");

        prompt.append("코드는 다음과 같습니다:\n");
        prompt.append("```").append(userCode).append("```\n\n");
        prompt.append("추가적인 프로젝트 정보(참고용): ").append(projectInfo).append("\n\n");

        prompt.append("지침:\n");
        prompt.append("1. 코드를 보고 사용된 언어와 라이브러리/프레임워크(추정 가능하다면) 및 핵심 CS 개념을 최대한 찾아내세요.\n");
        prompt.append("2. 그중 중요도나 면접 빈도가 높은 개념을 기준으로 3가지 질문을 만들되, ");
        prompt.append("가능하면 서로 다른 주제를 다루세요 (예: OOP/자료구조/알고리즘 등).\n");
        prompt.append("3. 각 질문은 해당 코드에서 어떤 부분이 그 CS 개념과 관련되는지 간단히 언급하며, ");
        prompt.append("개념 전반에 대한 이해를 확인할 수 있게 작성하세요.\n");
        prompt.append("4. 출력은 다음 JSON 배열 구조를 따라주세요. 질문 이외의 설명은 넣지 마세요.\n");
        prompt.append("[\n");
        prompt.append("  { \"question\": \"첫 번째 질문\" },\n");
        prompt.append("  { \"question\": \"두 번째 질문\" },\n");
        prompt.append("  { \"question\": \"세 번째 질문\" }\n");
        prompt.append("]\n");

        prompt.append("예) [ { \"question\": \"이 코드는 OO개념 중 상속을 어떻게 활용했나요?\" }, ... ]\n");
        prompt.append("출력은 위와 같은 JSON 배열 형태만 정확히 반환하세요.\n");

        return prompt.toString();
    }

    public String createPromptForAnswerProject(String userCode, String csQuestion, String answer) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 10년 경력의 시니어 소프트웨어 엔지니어이자 면접관입니다. ");
        prompt.append("아래에 제시된 코드와 질문, 그리고 사용자의 답변을 참고하여 ");
        prompt.append("이 질문에 대한 정확한 답변(해설)과, 사용자의 답변에 대한 피드백을 한국어로 작성해주세요.\n\n");
        prompt.append("코드:\n```").append(userCode).append("```\n\n");
        prompt.append("질문:\n").append(csQuestion).append("\n\n");
        prompt.append("사용자 답변:\n").append(answer).append("\n\n");
        
        prompt.append("지침:\n");
        prompt.append("1. 사용자의 답변이 충분하지 않거나 비어있는 경우, 질문에 알맞은 정답을 대신 제시해주세요.\n");
        prompt.append("2. 사용자의 답변이 어느 정도 맞았더라도, 추가 보완 사항이나 개선점을 피드백 형태로 명시해주세요.\n");
        prompt.append("3. 먼저 '정확한 답변'을 간략히 서술하고, 이어서 '사용자의 답변'에 대한 피드백을 작성해주세요.\n");
        prompt.append("4. 최종 출력은 JSON 배열 형태로만 반환해주세요. 예시는 아래와 같습니다.\n");
        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"feedback\": \"여기에 올바른 해설과 함께 사용자의 답변을 평가/보완하는 피드백을 서술\"\n");
        prompt.append("  }\n");
        prompt.append("]\n");
        prompt.append("JSON 외 다른 텍스트는 넣지 마세요.\n");

        return prompt.toString();
    }
}


