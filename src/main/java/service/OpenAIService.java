package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OpenAIService {

    @Value("${openai.api.key}")
    private String openAIApiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openAIApiUrl;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OpenAIService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * 법률 문서를 쉬운 말로 해석
     */
    public String interpretLegalDocument(String extractedText) throws AIInterpretationException {
        try {
            log.info("Starting AI interpretation for text length: {}", extractedText.length());

            String prompt = createLegalInterpretationPrompt(extractedText);
            String response = callChatGPTAPI(prompt);

            log.info("AI interpretation completed");
            return response;
        } catch (Exception e) {
            log.error("AI interpretation failed", e);
            throw new AIInterpretationException("AI interpretation failed: " + e.getMessage(), e);
        }
    }

    /**
     * ChatGPT API 호출
     */
    private String callChatGPTAPI(String prompt) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("max_tokens", 2000);
        requestBody.put("temperature", 0.7);

        try {
            String responseBody = webClient.post()
                    .uri(openAIApiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAIApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractContentFromResponse(responseBody);

        } catch (WebClientResponseException e) {
            log.error("OpenAI API call failed with status: {} and body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new Exception("OpenAI API call failed: " + e.getMessage());
        }
    }

    /**
     * 법률 문서 해석을 위한 프롬프트 생성
     */
    private String createLegalInterpretationPrompt(String legalText) {
        return String.format(
                """
                다음은 법률 문서에서 추출된 텍스트입니다. 이 내용을 일반인이 쉽게 이해할 수 있도록 해석해주세요.
                
                요구사항:
                1. 법률 용어를 쉬운 말로 바꿔서 설명
                2. 복잡한 문장을 간단하고 명확하게 표현
                3. 핵심 내용을 불렛 포인트로 정리
                4. 일반인의 시각에서 이해하기 쉽게 설명
                5. 한국어로 응답
                
                원본 법률 텍스트:
                %s
                
                위 내용을 쉽게 해석해주세요:
                """, legalText
        );
    }

    /**
     * OpenAI API 응답에서 내용 추출
     */
    private String extractContentFromResponse(String responseBody) throws Exception {
        try {
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            JsonNode choices = jsonResponse.get("choices");

            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null) {
                    JsonNode content = message.get("content");
                    if (content != null) {
                        return content.asText().trim();
                    }
                }
            }

            throw new Exception("Invalid response format from OpenAI API");

        } catch (Exception e) {
            log.error("Failed to parse OpenAI API response", e);
            throw new Exception("Failed to parse OpenAI API response: " + e.getMessage());
        }
    }

    /**
     * AI 해석 전용 예외 클래스
     */
    public static class AIInterpretationException extends Exception {
        public AIInterpretationException(String message) {
            super(message);
        }

        public AIInterpretationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
