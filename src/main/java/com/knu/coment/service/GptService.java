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
        prompt.append("아래 제공된 코드와 프로젝트 설명을 바탕으로, 기술 면접에 사용할 CS 기반 질문을 생성하세요.\n\n");

        prompt.append("전체 코드:\n");
        prompt.append("```").append(userCode).append("```\n\n");
        prompt.append("프로젝트 정보: ").append(projectInfo).append("\n\n");

        prompt.append("지침:\n");
        prompt.append("1. 전체 코드를 분석하고, 각 질문마다 해당 질문과 관련된 **코드 블록을 의미 단위로 넓게 발췌**해서 함께 제시하세요.\n");
        prompt.append("   - 예를 들어, 하나의 메서드 전체 또는 연관된 조건문, 반복문, try-catch 블록 등은 맥락을 유지하도록 함께 포함하세요.\n");
        prompt.append("   - 이 발췌된 코드는 반드시 userCode 내 원본 텍스트와 정확히 일치해야 합니다.\n");
        prompt.append("   - 단순 setter/getter, import, 불필요한 주석 등은 여전히 제외하세요.\n");
        prompt.append("2. 각 질문은 한국어로 작성하며, 아래 Enum 목록 중 하나의 category를 정확히 포함해야 합니다.\n");
        prompt.append("3. 질문 수는 총 3개이며, 서로 다른 주제를 다루도록 하세요.\n");
        prompt.append("4. 코드 발췌는 자료구조 사용, 동시성 처리, DB 접근, 알고리즘 로직 등 면접 질문으로 삼을 만한 핵심 내용을 중심으로 하되, **질문의 이해를 돕기 위해 연관된 코드 맥락을 충분히 포함**하세요.\n\n");


        prompt.append("사용 가능한 category 값 (정확히 복사):\n");
        prompt.append("[\n");
        prompt.append("  \"DATA_STRUCTURES_ALGORITHMS\",\n");
        prompt.append("  \"OPERATING_SYSTEMS\",\n");
        prompt.append("  \"NETWORKING\",\n");
        prompt.append("  \"DATABASES\",\n");
        prompt.append("  \"SECURITY\",\n");
        prompt.append("  \"LANGUAGE_AND_DEVELOPMENT_PRINCIPLES\",\n");
        prompt.append("]\n\n");

        prompt.append("출력 형식 (질문별 발췌 코드 포함):\n");
        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"question\": \"면접 질문 (한국어)\",\n");
        prompt.append("    \"category\": \"<Enum 상수명>\",\n");
        prompt.append("    \"relatedCode\": \"userCode 내 해당 질문과 직접 관련 있는 부분을 정확히 복사\"\n");
        prompt.append("  },\n");
        prompt.append("  ... (총 3개)\n");
        prompt.append("]\n\n");

        prompt.append("예시:\n");
        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"question\": \"HashMap을 캐시로 사용하는 것의 장단점은 무엇인가요?\",\n");
        prompt.append("    \"category\": \"DATA_STRUCTURES_ALGORITHMS\",\n");
        prompt.append("    \"relatedCode\": \"private final Map<String, User> cache = new HashMap<>();\"\n");
        prompt.append("  },\n");
        prompt.append("  {\n");
        prompt.append("    \"question\": \"이 코드에서 동시성 이슈가 발생할 수 있는 부분은 어디이며, 어떻게 해결할 수 있을까요?\",\n");
        prompt.append("    \"category\": \"OPERATING_SYSTEMS\",\n");
        prompt.append("    \"relatedCode\": \"public synchronized User getUser(String id) { return cache.get(id); }\"\n");
        prompt.append("  },\n");
        prompt.append("  {\n");
        prompt.append("    \"question\": \"이 메서드는 SOLID 원칙 중 어떤 부분을 위배하고 있나요?\",\n");
        prompt.append("    \"category\": \"NETWORKING\",\n");
        prompt.append("    \"relatedCode\": \"public void saveAndNotify(User user) { save(user); notifyAdmin(user); }\"\n");
        prompt.append("  }\n");
        prompt.append("]");

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


