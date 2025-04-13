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

        prompt.append("당신은 10년 경력의 시니어 소프트웨어 엔지니어이자 면접관입니다.\n")
                .append("다음에 주어지는 코드와 질문, 그리고 사용자가 작성한 답변을 참고하여, ")
                .append("정확한 해설과 함께 피드백을 제시해주세요. 만약 사용자의 답변이 비거나 매우 부족하다면, ")
                .append("예시처럼 상세한 답변을 직접 작성해주시기 바랍니다.\n\n");

        prompt.append("코드:\n```").append(userCode).append("```\n\n")
                .append("질문:\n").append(csQuestion).append("\n\n")
                .append("사용자 답변:\n").append(answer).append("\n\n");

        prompt.append("지침:\n")
                .append("1. 'feedback' 필드에 아래 예시처럼 **자세한 해설** 또는 **피드백**(사용자 답변 평가 및 보완점)을 작성해주세요.\n")
                .append("   - 사용자의 답변이 어느 정도 맞았으면, 해당 부분을 인정하고 추가 보완점을 제시해주세요.\n")
                .append("   - 사용자의 답변이 비어있거나 너무 짧으면, 직접 모범 답안을 작성해주세요.\n")
                .append("2. 최종 출력은 **JSON 배열** 형태로 한 개의 원소만 가지며, 아래 예시처럼 구성해주세요.\n")
                .append("3. JSON 배열 **이외의 텍스트를 추가하지 마세요**(설명, 마크다운 등 불필요한 출력 금지).\n\n");

        prompt.append("출력 예시 1:\n")
                .append("[\n")
                .append("  {\n")
                .append("    \"feedback\": \"TCP 3-way handshake는 ... (중략)\"\n")
                .append("  }\n")
                .append("]\n\n")
                .append("- 위 예시처럼 'feedback' 키 아래에 상세한 해설을 작성해주세요.\n")
                .append("- 사용자의 답변 일부가 적절하면 그 점을 칭찬 또는 인용하고, 잘못된 부분이 있으면 올바르게 정정하며 보완하세요.\n")
                .append("- 가능하다면 '정확한 답변'과 '사용자 답변 평가'가 구분되도록 서술해주세요.\n");

        return prompt.toString();
    }

}


