package law.counsel.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import law.counsel.analysis.dto.DocumentAnalysisResponse;
import law.counsel.analysis.dto.RiskAnalysisDto;
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

    public String improveTextReadability(String ocrText) throws AIInterpretationException {
        try {
            log.info("Starting text readability improvement for text length: {}", ocrText.length());
            
            String prompt = createReadabilityImprovementPrompt(ocrText);
            String response = callChatGPTAPI(prompt);
            
            log.info("Text readability improvement completed");
            return response;
        } catch (Exception e) {
            log.error("Text readability improvement failed", e);
            throw new AIInterpretationException("Text readability improvement failed: " + e.getMessage(), e);
        }
    }

    public DocumentAnalysisResponse analyzeDocumentRisks(String documentText) throws AIInterpretationException {
        try {
            log.info("Starting document risk analysis for text length: {}", documentText.length());
            
            String improvedText = improveTextReadability(documentText);
            List<RiskAnalysisDto> riskAnalyses = performRiskAnalysis(improvedText);
            
            DocumentAnalysisResponse response = new DocumentAnalysisResponse();
            response.setImprovedText(improvedText);
            response.setRiskAnalyses(riskAnalyses);
            
            log.info("Document risk analysis completed with {} risk items", riskAnalyses.size());
            return response;
        } catch (Exception e) {
            log.error("Document risk analysis failed", e);
            throw new AIInterpretationException("Document risk analysis failed: " + e.getMessage(), e);
        }
    }

    private List<RiskAnalysisDto> performRiskAnalysis(String documentText) throws Exception {
        String prompt = createRiskAnalysisPrompt(documentText);
        String response = callChatGPTAPI(prompt);
        
        try {
            return objectMapper.readValue(response, new TypeReference<List<RiskAnalysisDto>>() {});
        } catch (Exception e) {
            log.error("Failed to parse risk analysis response: {}", response);
            throw new Exception("Failed to parse risk analysis response: " + e.getMessage());
        }
    }

    private String createReadabilityImprovementPrompt(String ocrText) {
        return String.format(
                """
                다음은 OCR로 추출된 법률 문서 텍스트입니다. 이 텍스트의 가독성을 향상시켜 주세요.
                
                요구사항:
                1. OCR 오류로 인한 오타나 잘못된 문자를 수정
                2. 문단과 문장을 자연스럽게 정리
                3. 의미를 변경하지 않고 원본 내용 유지
                4. 법률 용어는 정확하게 보존
                5. 불필요한 줄바꿈이나 공백 정리
                6. 한국어로 응답
                
                OCR 텍스트:
                %s
                
                가독성이 향상된 텍스트만 반환해주세요 (추가 설명 없이):
                """, ocrText
        );
    }

    private String createRiskAnalysisPrompt(String documentText) {
        return String.format(
                """
                다음은 법률 계약서 텍스트입니다. 이 문서를 분석하여 위험조항을 탐지하고 쉬운말로 해석해주세요.
                
                요구사항:
                1. 각 문장을 분석하여 위험도를 HIGH, MEDIUM, LOW로 분류
                2. 위험한 조항에 대해서는 사유와 법령 근거 제시
                3. 일반인이 이해하기 쉬운 설명과 권장 문장 제공
                4. 결과를 JSON 배열 형태로 반환
                
                JSON 형식:
                [
                  {
                    "sentenceId": 숫자,
                    "riskLevel": "HIGH|MEDIUM|LOW",
                    "originalText": "원본 문장",
                    "riskReason": "위험 사유",
                    "legalReferences": ["관련 법령1", "관련 법령2"],
                    "simpleExplanation": "쉬운 설명",
                    "suggestedRevision": "권장 수정 문장"
                  }
                ]
                
                분석할 문서:
                %s
                
                위 형식의 JSON 배열만 반환해주세요 (추가 설명 없이):
                """, documentText
        );
    }

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

    public static class AIInterpretationException extends Exception {
        public AIInterpretationException(String message) {
            super(message);
        }

        public AIInterpretationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}