package com.knu.coment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knu.coment.config.GptConfig;
import com.knu.coment.global.CSCategory;
import com.knu.coment.global.Stack;
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

        prompt.append("지침:\n")
                .append("1. 전체 코드를 분석하여 3개의 서로 다른 주제에 대한 질문을 생성하세요.\n")
                .append("2. 각 질문에 대해 'relatedCode' 필드에 관련 코드 블록을 원본 그대로 정확히 복사해 포함하세요.\n")
                .append("   - 메서드 전체, 조건문·반복문·예외 처리 블록 등 맥락이 유지되도록 넓게 발췌합니다.\n")
                .append("   - 단순 getter/setter, import, 불필요한 주석 등은 제외하세요.\n")
                .append("3. 질문 카테고리는 아래 Enum 목록 중 하나를 정확히 사용하세요.\n")
                .append("4. 출력은 JSON 배열 형식으로, 배열 이외의 텍스트는 절대 추가하지 마세요.\n")
                .append("   - 불필요한 설명, 마크다운, 주석은 제거합니다.\n\n");


        prompt.append("사용 가능한 category 값:\n```")
                .append("DATA_STRUCTURES_ALGORITHMS\n")
                .append("OPERATING_SYSTEMS\n")
                .append("NETWORKING\n")
                .append("DATABASES\n")
                .append("SECURITY\n")
                .append("LANGUAGE_AND_DEVELOPMENT_PRINCIPLES\n")
                .append("ETC\n")
                .append("```\n\n");

        prompt.append("출력 형식:\n```json\n")
                .append("[\n")
                .append("  {\n")
                .append("    \"question\": \"<면접 질문 (한국어)>\",\n")
                .append("    \"category\": \"<Enum 상수명>\",\n")
                .append("    \"relatedCode\": \"<관련 코드 블록 정확히 복사>\"\n")
                .append("  },\n")
                .append("  ... (총 3개 질문)\n")
                .append("]\n```");

        prompt.append("\n예시:\n```json\n")
                .append("[\n")
                .append("  {\n")
                .append("    \"question\": \"HashMap을 캐시로 사용하는 장단점은 무엇인가요?\",\n")
                .append("    \"category\": \"DATA_STRUCTURES_ALGORITHMS\",\n")
                .append("    \"relatedCode\": \"private final Map<String, User> cache = new HashMap<>();\"\n")
                .append("  },\n")
                .append("  {\n")
                .append("    \"question\": \"이 코드에서 동시성 이슈가 발생할 수 있는 부분은 어디이며, 어떻게 해결할 수 있나요?\",\n")
                .append("    \"category\": \"OPERATING_SYSTEMS\",\n")
                .append("    \"relatedCode\": \"public synchronized User getUser(String id) { return cache.get(id); }\"\n")
                .append("  },\n")
                .append("  {\n")
                .append("    \"question\": \"이 메서드는 SOLID 원칙 중 어떤 부분을 위배하고 있나요?\",\n")
                .append("    \"category\": \"LANGUAGE_AND_DEVELOPMENT_PRINCIPLES\",\n")
                .append("    \"relatedCode\": \"public void saveAndNotify(User user) { save(user); notifyAdmin(user); }\"\n")
                .append("  }\n")
                .append("]\n```");

        return prompt.toString();
    }


    public String createPromptForAnswerProject(String userCode, String csQuestion, String answer) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("당신은 10년 경력의 시니어 소프트웨어 엔지니어이자 면접관입니다.\n")
                .append("아래 제공된 코드, 질문, 그리고 사용자의 답변을 참고하여, 정확한 해설과 피드백을 제시하세요.\n\n");

        prompt.append("코드:\n```").append(userCode).append("```\n\n")
                .append("질문:\n").append(csQuestion).append("\n\n")
                .append("사용자 답변:\n").append(answer).append("\n\n");

        prompt.append("#### 지침\n")
                .append("1. 출력은 **JSON 배열** 한 개 원소만, 배열 외 불필요한 텍스트는 절대 추가하지 마세요.\n")
                .append("2. `feedback` 필드 안에 다음 마크다운 구조를 사용하세요:\n")
                .append("   ```markdown\n")
                .append("   ## ✅ 정확한 답변\n")
                .append("   - 면접 시 답변하듯 **상세하게**, 고려한 과정과 근거를 포함하여 설명하세요.\n\n")
                .append("   ## 📝 사용자 답변 평가 및 보완점\n")
                .append("   - 사용자의 답변이 **비어있거나 완전히 틀렸다면**, 칭찬은 생략하고 바로 보완 설명을 제시하세요.\n")
                .append("   - 사용자의 답변이 일부 맞았다면, 그 부분만 간단히 언급하고 나머지 보완점을 상세히 작성하세요.\n")
                .append("   ```\n\n");

        prompt.append("#### 예시 출력\n")
                .append("```json\n")
                .append("[\n")
                .append("  {\n")
                .append("    \"feedback\": \"## ✅ 정확한 답변\\n")
                .append("- 힙 정렬은 힙 자료구조를 활용해 배열을 최대 힙으로 만든 뒤, 루트 값을 교환하고 힙을 재구성하는 과정을 반복합니다. 이로써 전체 정렬이 O(n log n)에 수행됩니다.\\n\\n")
                .append("## 📝 사용자 답변 평가 및 보완점\\n")
                .append("- 사용자의 답변이 비어있거나 완전히 틀려서, 올바른 힙 정렬 과정을 단계별로 설명했습니다.\"\n")
                .append("  }\n")
                .append("]\n")
                .append("```");

        prompt.append("위 예시처럼 'feedback' 키 아래에 상세한 해설을 작성하고, 필요 시 사용자 답변의 장단점을 평가하세요.");

        return prompt.toString();
    }
    public String createPromptForAnswerCS(
            Stack stack,
            CSCategory csCategory,
            String question,
            String answer
    ) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("당신은 10년 경력의 시니어 소프트웨어 엔지니어이자 면접관입니다.\n")
                .append("아래 제공된 사용자 스택, 질문 카테고리, 질문(및 포함된 코드 예시), 그리고 사용자가 작성한 답변을 참고하여, 정확한 해설과 피드백을 제시하세요.\n\n");

        prompt.append("사용자 스택\n```").append(stack).append("```\n\n")
                .append("질문 카테고리:\n```").append(csCategory).append("```\n\n")
                .append("질문:\n```").append(question).append("```\n\n")
                .append("사용자 답변:\n```").append(answer).append("```\n\n");

        prompt.append("#### 지침\n")
                .append("1. 피드백 설명에서 **코드 예시가 도움이 될 것으로 판단되면**, `## 🧩 관련 코드 예시` 섹션에 포함하세요.\n")
                .append("2. 출력은 **JSON 배열** 한 개 원소만, 배열 외 불필요한 텍스트는 절대 추가하지 마세요.\n")
                .append("3. `feedback` 필드 안에 다음 마크다운 구조를 사용하세요:\n")
                .append("   ```markdown\n")
                .append("   ## ✅ 정확한 답변\n")
                .append("   - 면접 시 답변하듯 **상세하게**, 고려한 과정과 근거를 포함하여 설명하세요.\n\n")
                .append("   ## 🧩 관련 코드 예시\n")
                .append("   ```java\n")
                .append("   - 설명에 도움이 되는 코드 예시가 있다면 여기에 작성하세요.\n")
                .append("   - 사용자 스택에 적합한 코드 예시를 제시하세요.\n")
                .append("   ```\n\n")
                .append("   ## 📝 사용자 답변 평가 및 보완점\n")
                .append("   - 사용자의 답변이 **비어있거나 완전히 틀렸다면**, 칭찬은 생략하고 바로 보완 설명을 제시하세요.\n")
                .append("   - 사용자의 답변이 일부 맞았다면, 그 부분만 간단히 언급하고 나머지 보완점을 상세히 작성하세요.\n")
                .append("   ```\n\n");

        prompt.append("#### 예시 출력\n")
                .append("```json\n")
                .append("[\n")
                .append("  {\n")
                .append("    \"feedback\": \"## ✅ 정확한 답변\\n")
                .append("- 힙 정렬은 힙 자료구조를 활용해 배열을 최대 힙으로 만든 뒤, 루트 값을 교환하고 힙을 재구성하는 과정을 반복합니다. 이로써 전체 정렬이 O(n log n)에 수행됩니다.\\n\\n")
                .append("## 🧩 관련 코드 예시\\n")
                .append("```java\\n")
                .append("public void heapSort(int[] arr) {\\n")
                .append("    int n = arr.length;\\n")
                .append("    for (int i = n / 2 - 1; i >= 0; i--) heapify(arr, n, i);\\n")
                .append("    for (int i = n - 1; i > 0; i--) {\\n")
                .append("        swap(arr, 0, i); heapify(arr, i, 0);\\n")
                .append("    }\\n")
                .append("}\\n")
                .append("```\\n\\n")
                .append("## 📝 사용자 답변 평가 및 보완점\\n")
                .append("- 사용자의 답변이 비어있거나 완전히 틀려서, 올바른 힙 정렬 과정을 단계별로 설명했습니다.\"\n")
                .append("  }\n")
                .append("]\n")
                .append("```");

        return prompt.toString();
    }

}


