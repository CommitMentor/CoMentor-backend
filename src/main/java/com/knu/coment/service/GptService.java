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

    private static final String SYSTEM_POLICY = String.join("\n",
            "당신은 10년 경력의 시니어 소프트웨어 엔지니어이자 면접관입니다.",
            "한국어로만 답하십시오.",
            "요청된 형식(JSON 배열, 필드명, 마크다운 헤더 등)을 엄격히 준수하십시오.",
            "배열 이외의 불필요한 텍스트/마크다운/설명/백틱을 출력하지 마십시오.",
            "JSON 문자열은 개행/따옴표/백틱을 올바르게 이스케이프하십시오."
    );

    public String callGptApi(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", gptConfig.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_POLICY),
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpEntity<Map<String, Object>> requestEntity =
                new HttpEntity<>(requestBody, gptConfig.httpHeaders());

        String gptApiUrl = "https://api.openai.com/v1/chat/completions";
        RestTemplate restTemplate = gptConfig.restTemplate();

        try {
            ResponseEntity<String> responseEntity =
                    restTemplate.postForEntity(gptApiUrl, requestEntity, String.class);
            String responseBody = responseEntity.getBody();

            if (responseBody == null || responseBody.isBlank()) {
                return "No response from GPT API.";
            }
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).path("message").path("content").asText(null);
                return (content == null || content.isBlank())
                        ? "No response from GPT API."
                        : content.trim();
            }
            return "No response from GPT API.";
        } catch (Exception e) {
            return "Error processing GPT API response.";
        }
    }

    /** 1) 프로젝트 기반 질문 생성 프롬프트 */
    public String createPromptForProject(String userCode, String projectInfo) {
        StringBuilder p = new StringBuilder();

        p.append("아래 코드와 프로젝트 설명을 바탕으로 기술 면접용 CS 질문을 생성하세요. 한국어로만 답하십시오.\n\n");
        p.append("전체 코드:\n```").append(userCode).append("```\n\n");
        p.append("프로젝트 정보:\n").append(projectInfo).append("\n\n");

        p.append("### 지침\n")
                .append("1) 서로 다른 **3개 카테고리**에 대해 질문을 생성합니다.\n")
                .append("2) 각 항목의 `relatedCode`에는 **질문과 직접 연관된 원본 코드**를 포함합니다.\n")
                .append("   - 메서드 전체/조건문·반복문·예외 처리 블록 등 **맥락이 유지되도록 넓게** 발췌\n")
                .append("   - getter/setter, import, 불필요 주석 제외\n")
                .append("   - 너무 길면 **핵심 80줄 이내**로 자르되 의미가 끊기지 않게 발췌\n")
                .append("3) `category`는 아래 Enum 중 하나만 사용합니다. 애매하면 `ETC`를 사용합니다.\n")
                .append("4) **반드시 유효한 JSON 배열**만 출력합니다. 배열 외 텍스트/마크다운/설명/백틱 금지.\n")
                .append("5) JSON 문자열 내 개행/따옴표/백틱을 올바르게 이스케이프하세요.\n\n");

        p.append("사용 가능한 category 값:\n```")
                .append("DATA_STRUCTURES_ALGORITHMS\n")
                .append("OPERATING_SYSTEMS\n")
                .append("NETWORKING\n")
                .append("DATABASES\n")
                .append("SECURITY\n")
                .append("LANGUAGE_AND_DEVELOPMENT_PRINCIPLES\n")
                .append("ETC\n")
                .append("```\n\n");

        p.append("출력 형식(스키마):\n```json\n")
                .append("[\n")
                .append("  {\n")
                .append("    \"question\": \"<면접 질문 (한국어)>\",\n")
                .append("    \"category\": \"<위 Enum 중 하나>\",\n")
                .append("    \"relatedCode\": \"<관련 코드 블록(핵심 80줄 이내, 문자열로 이스케이프)>\"\n")
                .append("  },\n")
                .append("  { ... },\n")
                .append("  { ... }\n")
                .append("]\n")
                .append("```\n");

        return p.toString();
    }

    /** 2) 프로젝트 Q&A 해설+피드백 (사용자 답변 있음) */
    public String createPromptForAnswerProject(String userCode, String csQuestion, String answer) {
        StringBuilder p = new StringBuilder();

        p.append("아래 코드/질문/사용자 답변을 참고하여 **정확한 해설과 피드백**을 한국어로만 제시하세요.\n\n");
        p.append("코드:\n```").append(userCode).append("```\n\n");
        p.append("질문:\n").append(csQuestion).append("\n\n");
        p.append("사용자 답변:\n").append(answer).append("\n\n");

        p.append("### 지침\n")
                .append("1) **반드시 JSON 배열(원소 1개)**만 출력합니다. 배열 외 텍스트/마크다운/설명/백틱 금지.\n")
                .append("2) `feedback` 값에는 아래 **전문가 템플릿**을 그대로 채워 넣습니다(헤더는 `###`).\n")
                .append("   ```markdown\n")
                .append("   ### 요약\n")
                .append("   - 한 줄 핵심: {핵심 정답 1문장}\n")
                .append("   - 왜 중요한가: {업무/성능/안전 관점 1~2문장}\n\n")
                .append("   ### 정답\n")
                .append("   - {핵심 개념·원리·근거를 단계적으로 4~6줄}\n\n")
                .append("   ### 코드 예시\n")
                .append("   ```언어\n")
                .append("   {핵심만 담은 15~25줄 샘플 코드}\n")
                .append("   ```\n\n")
                .append("   ### 정확성 점검(체크리스트)\n")
                .append("   - [ ] 핵심 조건 A\n")
                .append("   - [ ] 경계/에러/동시성 등 비기능 요구 고려\n")
                .append("   - [ ] 복잡도/리소스 근거\n")
                .append("   - [ ] 대안 비교(2~3줄)\n\n")
                .append("   ### 사용자의 답변 평가 및 보완\n")
                .append("   - 잘한 점: {사실 기반 1~2줄}\n")
                .append("   - 보완점(중요도 순):\n")
                .append("     1) {가장 큰 갭: 왜 문제인지/어떻게 고칠지}\n")
                .append("     2) {두 번째 갭: 실전 팁/근거}\n")
                .append("     3) {선택 개선: 코드/설계 트레이드오프}\n\n")
                .append("   ### 실무 팁\n")
                .append("   - {운영 이슈/모니터링/실패 패턴 2~3개}\n")
                .append("   - {툴/옵션/플래그 등 구체 팁 1~2개}\n\n")
                .append("   ### 확장 학습\n")
                .append("   - 키워드: `{3~5개}`\n")
                .append("   - 더 파기: {후속 주제 1~2개}\n")
                .append("   ```\n\n")
                .append("3) 섹션 길이 가이드: 요약 2–3줄, 정답 4–6줄, 코드 15–25줄, 체크리스트 3–4항목, 평가·보완 4–8줄, 실무 팁 2–4줄, 확장 학습 1–2줄.\n")
                .append("4) JSON 문자열 이스케이프를 준수하세요.\n");

        p.append("### 출력 예시\n")
                .append("```json\n")
                .append("[{\"feedback\":\"### 요약\\n- 한 줄 핵심: ...\"}]\n")
                .append("```\n");

        return p.toString();
    }

    /** 3) 프로젝트 Q&A 해설(사용자 답변 없음) */
    public String createPromptForAnswerProject2(String userCode, String csQuestion) {
        StringBuilder p = new StringBuilder();

        p.append("아래 코드와 질문을 참고하여 **정확한 해설**을 한국어로만 제시하세요.\n\n");
        p.append("코드:\n```").append(userCode).append("```\n\n");
        p.append("질문:\n").append(csQuestion).append("\n\n");

        p.append("### 지침\n")
                .append("1) **반드시 JSON 배열(원소 1개)**만 출력합니다. 배열 외 텍스트/마크다운/설명/백틱 금지.\n")
                .append("2) `feedback` 값에는 아래 **전문가 템플릿**을 그대로 채워 넣습니다(헤더는 `###`).\n")
                .append("   ```markdown\n")
                .append("   ### 요약\n")
                .append("   - 한 줄 핵심: {핵심 정답 1문장}\n")
                .append("   - 왜 중요한가: {업무/성능/안전 관점 1~2문장}\n\n")
                .append("   ### 정답\n")
                .append("   - {핵심 개념·원리·근거를 단계적으로 4~6줄}\n\n")
                .append("   ### 코드 예시\n")
                .append("   ```언어\n")
                .append("   {핵심만 담은 15~25줄 샘플 코드}\n")
                .append("   ```\n\n")
                .append("   ### 📝 추가 정보\n")
                .append("   - 핵심 개념과 인접 개념을 체계적으로 확장 설명(2~4줄)\n")
                .append("   ```\n\n")
                .append("3) JSON 문자열 이스케이프를 준수하세요.\n");

        p.append("### 출력 예시\n")
                .append("```json\n")
                .append("[{\"feedback\":\"### 요약\\n- 한 줄 핵심: ...\"}]\n")
                .append("```\n");

        return p.toString();
    }

    /** 4) CS 카테고리 기반 해설+피드백 (사용자 답변 있음) */
    public String createPromptForAnswerCS(
            Stack stack,
            CSCategory csCategory,
            String question,
            String answer
    ) {
        StringBuilder p = new StringBuilder();

        p.append("아래 사용자 스택/질문 카테고리/질문/사용자 답변을 참고하여 **정확한 해설과 피드백**을 한국어로만 제시하세요.\n\n");
        p.append("사용자 스택:\n```").append(stack).append("```\n\n");
        p.append("질문 카테고리:\n```").append(csCategory).append("```\n\n");
        p.append("질문:\n```").append(question).append("```\n\n");
        p.append("사용자 답변:\n```").append(answer).append("```\n\n");

        p.append("### 지침\n")
                .append("1) **반드시 JSON 배열(원소 1개)**만 출력합니다. 배열 외 텍스트/마크다운/설명/백틱 금지.\n")
                .append("2) 필요 시 `### 코드 예시`를 포함하되 **사용자 스택에 맞춘 언어/프레임워크**로 제시하세요(15–25줄).\n")
                .append("3) `feedback` 값에는 아래 **전문가 템플릿**을 그대로 채워 넣습니다(헤더는 `###`).\n")
                .append("   ```markdown\n")
                .append("   ### 요약\n")
                .append("   - 한 줄 핵심: {핵심 정답 1문장}\n")
                .append("   - 왜 중요한가: {업무/성능/안전 관점 1~2문장}\n\n")
                .append("   ### 정답\n")
                .append("   - {핵심 개념·원리·근거를 단계적으로 4~6줄}\n\n")
                .append("   ### 코드 예시\n")
                .append("   ```언어\n")
                .append("   {핵심만 담은 15~25줄 샘플 코드}\n")
                .append("   ```\n\n")
                .append("   ### 사용자의 답변 평가 및 보완\n")
                .append("   - 잘한 점: {사실 기반 1~2줄}\n")
                .append("   - 보완점(중요도 순): 1) 2) 3)\n")
                .append("   ```\n\n")
                .append("4) JSON 문자열 이스케이프를 준수하세요.\n");

        p.append("### 출력 예시\n")
                .append("```json\n")
                .append("[{\"feedback\":\"### 요약\\n- 한 줄 핵심: ...\"}]\n")
                .append("```\n");

        return p.toString();
    }

    /** 5) CS 카테고리 기반 해설(사용자 답변 없음) */
    public String createPromptForAnswerCS2(
            Stack stack,
            CSCategory csCategory,
            String question
    ) {
        StringBuilder p = new StringBuilder();

        p.append("아래 사용자 스택/질문 카테고리/질문을 참고하여 **정확한 해설**을 한국어로만 제시하세요.\n\n");
        p.append("사용자 스택:\n```").append(stack).append("```\n\n");
        p.append("질문 카테고리:\n```").append(csCategory).append("```\n\n");
        p.append("질문:\n```").append(question).append("```\n\n");

        p.append("### 지침\n")
                .append("1) **반드시 JSON 배열(원소 1개)**만 출력합니다. 배열 외 텍스트/마크다운/설명/백틱 금지.\n")
                .append("2) 필요 시 `### 코드 예시`를 포함하되 **사용자 스택에 맞춘 언어/프레임워크**로 제시하세요(15–25줄).\n")
                .append("3) `feedback` 값에는 아래 **전문가 템플릿**을 그대로 채워 넣습니다(헤더는 `###`).\n")
                .append("   ```markdown\n")
                .append("   ### 요약\n")
                .append("   - 한 줄 핵심: {핵심 정답 1문장}\n")
                .append("   - 왜 중요한가: {업무/성능/안전 관점 1~2문장}\n\n")
                .append("   ### 정답\n")
                .append("   - {핵심 개념·원리·근거를 단계적으로 4~6줄}\n\n")
                .append("   ### 코드 예시\n")
                .append("   ```언어\n")
                .append("   {핵심만 담은 15~25줄 샘플 코드}\n")
                .append("   ```\n\n")
                .append("   ### 📝 추가 정보\n")
                .append("   - 핵심 개념과 인접 개념을 체계적으로 확장 설명(2~4줄)\n")
                .append("   ```\n\n")
                .append("4) JSON 문자열 이스케이프를 준수하세요.\n");

        p.append("### 출력 예시\n")
                .append("```json\n")
                .append("[{\"feedback\":\"### 요약\\n- 한 줄 핵심: ...\"}]\n")
                .append("```\n");

        return p.toString();
    }
}
